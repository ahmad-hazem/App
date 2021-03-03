package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.app.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class Sign_Up extends AppCompatActivity {
    ActivitySignUpBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference databaseReferenceUserRoot;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(Sign_Up.this,R.layout.activity_sign__up);
        
        init();
        initListnenr();

    }

    private void init() {
        FirebaseAuth.getInstance();
        databaseReferenceUserRoot= FirebaseDatabase.getInstance().getReference().child(Constance.UserRoot);
        dialog = new ProgressDialog(getBaseContext());
        dialog.setCancelable(false);
        dialog.setMessage("Wait ...");
    }

    private void initListnenr() {

        onRegisterClick();
        onLoginClick();
        selectImage();
    }

    private void selectImage() {
        binding.ivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(Sign_Up.this);

            }
        });

    }

    private void onLoginClick() {
        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void onRegisterClick() {
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setErrorNull(binding.etFullName,binding.etEmail,binding.etPhone,binding.etPassword);
                if (isEmptyEditText(binding.etFullName)){
                    setError(binding.etFullName,"Required");
                    return;
                }

                if (isEmptyEditText(binding.etPhone)){
                    setError(binding.etPhone,"Required");
                    return;
                }

                if (isEmptyEditText(binding.etEmail)){
                    setError(binding.etEmail,"Required");
                    return;
                }

                if (!getTextFromET(binding.etEmail).contains("@")){
                    setError(binding.etEmail,"Example@ Example.com");
                    return;
                }

                if (isEmptyEditText(binding.etPassword)){
                    setError(binding.etPassword,"Required");
                    return;
                }

                if (getTextFromET(binding.etPassword).length()<8){
                    setError(binding.etPassword,"Password too short, At least 8 character");
                    return;
                }

                String email = getTextFromET(binding.etEmail);
                String password= getTextFromET(binding.etPassword);
                String name= getTextFromET(binding.etFullName);
                String phone = getTextFromET(binding.etPhone);
                register(email, password, name, phone);
            }


        });


    }

    private void register(String email, String password, String name, String phone) {

        try {
            dialog.show();
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            String userId= auth.getCurrentUser().getUid();
                            Map <String, Object> map = new HashMap<>();
                            map.put("userID", userId);
                            map.put("UserName",name);
                            map.put("Phone",phone);
                            databaseReferenceUserRoot.child(userId).setValue(map);
                            dialog.dismiss();
                            finish();
                            Toast.makeText(Sign_Up.this, "Success", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(Sign_Up.this, "Fail", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setErrorNull(EditText etFullName, EditText etEmail, EditText etPhone, EditText etPassword) {
        etFullName.setError(null);
        etEmail.setError(null);
        etPhone.setError(null);
        etPhone.setError(null);
    }

    private boolean isEmptyEditText(EditText editText){
        if (TextUtils.isEmpty(editText.getText())){
            return true;
        }
        else {
            return false;
        }

    }

    private String getTextFromET(EditText editText){
        return editText.getText().toString();
    }

    public static void setError(EditText editText, String error){
        editText.setError(error);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                binding.ivUserImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}