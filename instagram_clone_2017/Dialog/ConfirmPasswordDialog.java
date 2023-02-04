package com.example.instagram_clone_2017.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.instagram_clone_2017.R;

import org.w3c.dom.Text;

public class ConfirmPasswordDialog extends DialogFragment {

    public interface OnConfirmPasswordListener
    {
        public void onConfirmPassword(String password);
    }

    OnConfirmPasswordListener mOnConfirmPasswordListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
        TextView cancelDialog = view.findViewById(R.id.dialog_cancel);
        TextView confirmDialog = view.findViewById(R.id.dialog_confirm);
        EditText confirmPassword = view.findViewById(R.id.confirm_password);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!confirmPassword.getText().toString().equals(""))
                {
                    mOnConfirmPasswordListener.onConfirmPassword(confirmPassword.getText().toString());
                    getDialog().dismiss();
                }
                else
                {
                    Toast.makeText(getActivity(), "You Must Enter Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mOnConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();
    }
}
