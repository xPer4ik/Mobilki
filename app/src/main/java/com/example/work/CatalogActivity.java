package com.example.work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.work.databinding.ActivityCatalogBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CatalogActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private boolean hasAvatar = false;
    private String avatarUrl = null;
    private ActivityCatalogBinding binding;
    private ListView listView;
    private ArrayList<Product> productList;
    private ImageView avatarImageView;
    private Uri avatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCatalogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Каталог товаров");
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.action_bar_avatar);
            avatarImageView = actionBar.getCustomView().findViewById(R.id.action_bar_avatar);
        }

        listView = binding.listView;
        productList = new ArrayList<>();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://work-6f0a9.appspot.com");
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://work-6f0a9-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference productsReference = database.getReference("Products/");

        productsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    if (productSnapshot.exists()) {
                        String productName = productSnapshot.child("name").getValue(String.class);
                        String productDesc = productSnapshot.child("desc").getValue(String.class);
                        int productCost = productSnapshot.child("price").getValue(Integer.class);
                        String imageUrl = productSnapshot.child("img").getValue(String.class);
                        StorageReference imageRef = storage.getReference(imageUrl);
                        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Product product = new Product(productName, productCost, bitmap, productDesc);
                            productList.add(product);

                            if (productList.size() == dataSnapshot.getChildrenCount()) {
                                ProductAdapter boxAdapter = new ProductAdapter(CatalogActivity.this, productList);
                                listView.setAdapter(boxAdapter);
                            }
                        }).addOnFailureListener(exception -> {
                            Log.e("TAG", "Failed to load image from Firebase Storage", exception);
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = (Product) parent.getItemAtPosition(position);
            openProductDetailFragment(selectedProduct);
        });
        loadAvatarFromLocal();
    }
    private void loadAvatarFromLocal() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String avatarLocalPath = sharedPreferences.getString("avatar_local_path", null);
        if (avatarLocalPath != null) {
            Bitmap avatarBitmap = BitmapFactory.decodeFile(avatarLocalPath);
            avatarImageView.setImageBitmap(avatarBitmap);
        }
    }

    private void openProductDetailFragment(Product product) {
        Bitmap productImage = product.getImg();
        String productName = product.getName();
        String productDesc = product.getDesc();
        int productPrice = product.getPrice();

        Fragment productDetailFragment = ProductDetailFragment.newInstance(productName, productPrice, productImage, productDesc);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, productDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_for_bar, menu);
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "None");
        Log.d("myLogs", role);
        if (role.equals("admin")) {
            menu.add(Menu.NONE, R.id.action_additional_item, Menu.NONE, "Добавить администратора");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            handleLogout();
            return true;
        }
        if (id == R.id.action_about_author) {
            AuthorFragment dialogFragment = new AuthorFragment();
            dialogFragment.show(getSupportFragmentManager(), "AuthorDialogFragment");
            return true;
        } else if (id == R.id.action_user_instruction) {
            showUserInstructionDialog();
            return true;
        } else if (id == R.id.action_about_app) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_additional_item) {
            showCustomDialog();
            return true;
        } else if (id == R.id.action_avatar) {
            showAvatarOptionsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void handleLogout() {

        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("avatar_local_path");
        editor.apply();

        // Удаляем сам файл из файловой системы устройства
        String avatarLocalPath = sharedPreferences.getString("avatar_local_path", null);
        if (avatarLocalPath != null) {
            File avatarFile = new File(avatarLocalPath);
            if (avatarFile.exists()) {
                avatarFile.delete();
            }
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        editor.putBoolean("is_logged_in", false);
        editor.apply();

        // Redirect to login activity
        Intent intent = new Intent(CatalogActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("О программе");
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "None");
        Log.d("myLogs", role);
        if (role.equals("admin")) {
            builder.setMessage("Версия 2.0.0");
        } else {
            builder.setMessage("Версия: 1.0.0");
        }
        builder.setPositiveButton("Закрыть", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUserInstructionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Инструкция пользователю");
        builder.setMessage("Выберите нужный товар и закажите");
        builder.setPositiveButton("Закрыть", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.edit_text_name);
        EditText editTextEmail = dialogView.findViewById(R.id.edit_text_email);
        EditText editTextPassword = dialogView.findViewById(R.id.edit_text_password);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = editTextName.getText().toString();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            addAdmin(email, password, name, "admin");
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addAdmin(String email, String password, String username, String role) {
        FirebaseAuth mAuth;
        DatabaseReference usersRef;
        FirebaseDatabase db;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://work-6f0a9-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = db.getReference("Users");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CatalogActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        String userId = mAuth.getCurrentUser().getUid();
                        User user = new User(username, email, role);
                        usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(unused -> {
                                })
                                .addOnFailureListener(e -> {
                                });
                    } else {
                        Toast.makeText(CatalogActivity.this, "Ошибка входа:: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAvatarOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите источник изображения")
                .setItems(new String[]{"Камера", "Галерея"}, (dialog, which) -> {
                    if (which == 0) {
                        if (isEmulator()) {
                            Toast.makeText(this, "Камера недоступна на эмуляторе", Toast.LENGTH_SHORT).show();
                        } else {
                            openCamera();
                        }
                    } else {
                        openGallery();
                    }
                });
        builder.create().show();
    }
    private boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("sdk_gphone")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.toLowerCase().contains("emulator")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk".equals(Build.PRODUCT);
    }
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                avatarImageView.setImageBitmap(photo);
                saveAvatarLocally(photo);
            } else if (requestCode == REQUEST_GALLERY) {
                avatarUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), avatarUri);
                    avatarImageView.setImageBitmap(bitmap);
                    saveAvatarLocally(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void saveAvatarLocally(Bitmap avatarBitmap) {
        // Сохраняем изображение в файловой системе устройства
        String fileName = "avatar_image.png";
        try {
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            // Сохраняем путь к файлу в SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("avatar_local_path", getFilesDir() + "/" + fileName);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
