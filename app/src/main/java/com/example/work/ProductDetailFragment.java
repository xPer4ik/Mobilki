package com.example.work;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.work.databinding.FragmentProductDetailBinding;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ProductDetailFragment extends Fragment {
    private static final String ARG_NAME = "name";
    private static final String ARG_PRICE = "price";
    private static final String ARG_IMAGE = "image";
    private static final String ARG_DESC = "desc";
    private FragmentProductDetailBinding binding;
    private int mDefaultColor = 0;

    public static ProductDetailFragment newInstance(String name, int price, Bitmap image, String desc) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_DESC, desc);
        args.putInt(ARG_PRICE, price);
        args.putParcelable(ARG_IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Получение данных из аргументов
        if (getArguments() != null) {
            String name = getArguments().getString(ARG_NAME);
            String desc = getArguments().getString(ARG_DESC);
            int price = getArguments().getInt(ARG_PRICE);
            Bitmap image = getArguments().getParcelable(ARG_IMAGE);

            // Установка данных в элементы макета
            binding.productDetailName.setText(name);
            binding.productDetailDesc.setText(desc);
            binding.productDetailPrice.setText("Цена " + String.valueOf(price) + "руб.");
            binding.productDetailImage.setImageBitmap(image);
        }

        // Обработка нажатия кнопки закрытия
        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Обработка нажатия кнопки выбора цвета
        binding.pickColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPickerDialogue();
            }
        });

        // Обработка нажатия кнопки установки цвета
        binding.setColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //binding.productDetailName.setTextColor(mDefaultColor);
                Toast.makeText(getActivity(), "Покупка совершена", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openColorPickerDialogue() {
        final AmbilWarnaDialog colorPickerDialogue = new AmbilWarnaDialog(getContext(), mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // Do nothing on cancel
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor = color;
                binding.previewSelectedColor.setBackgroundColor(mDefaultColor);
            }
        });
        colorPickerDialogue.show();
    }
}
