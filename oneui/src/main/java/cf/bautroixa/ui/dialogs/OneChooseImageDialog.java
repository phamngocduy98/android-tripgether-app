package cf.bautroixa.ui.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cf.bautroixa.ui.R;
import cf.bautroixa.ui.helpers.ImageHelper;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class OneChooseImageDialog extends OneDialog {
    private OnImagePickedListener onImagePickedListener = null;

    public interface RequestCodeDefault {
        int ONE_CHOOSE_IMAGE_DIALOG = 120001;
        int TAKE_A_PICTURE = 120002;
        int PICK_FROM_GALLERY = 120003;
    }

    View linearCamera, linearGallery;

    public OneChooseImageDialog() {
    }

    public void setOnImagePickedListener(OnImagePickedListener onImagePickedListener) {
        this.onImagePickedListener = onImagePickedListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View body = inflater.inflate(R.layout.one_dialog_chooser_image_body, container, false);
        setCustomBody(body);
        setTitleRes(R.string.dialog_title_choose_image);
        setPosBtnRes(R.string.btn_cancel);
        setButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearCamera = view.findViewById(R.id.linear_camera);
        linearGallery = view.findViewById(R.id.linear_gallery);

        linearCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, RequestCodeDefault.TAKE_A_PICTURE);
            }
        });

        linearGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, RequestCodeDefault.PICK_FROM_GALLERY);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (onImagePickedListener == null) {
            throw new IllegalStateException("No OnImagePickedListener attached to " + this + ".");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case RequestCodeDefault.TAKE_A_PICTURE:
                    if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                        Bitmap selectedImageBitmap = (Bitmap) data.getExtras().get("data");
                        onImagePickedListener.onPicked(null, selectedImageBitmap);
                        dismiss();
                        return;
                    }
                    break;
                case RequestCodeDefault.PICK_FROM_GALLERY:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap = ImageHelper.getLocalImageFromUri(requireContext(), selectedImageUri);
                        onImagePickedListener.onPicked(selectedImageUri, selectedImageBitmap);
                        dismiss();
                        return;
                    }
                    break;
            }
        }
    }

    public interface OnImagePickedListener {
        void onPicked(@Nullable Uri uri, Bitmap bitmap);
    }
}
