package com.jasonmccoy.a7leavescardx.fragments;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jasonmccoy.a7leavescardx.Helper;
import com.jasonmccoy.a7leavescardx.R;
import com.jasonmccoy.a7leavescardx.events.ProfileFinishEvent;
import com.jasonmccoy.a7leavescardx.items.User;
import com.jasonmccoy.a7leavescardx.services.SaveProfileService;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_BIRTHDAY;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_EMAIL;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_GENDER;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_INFO_PREFERENCES;
import static com.jasonmccoy.a7leavescardx.LoginActivity.USER_NAME;


public class ProfileFragment extends Fragment {

    public static final String TAG = TEST + ProfileFragment.class.getSimpleName();

    private static final int PICK_IMAGE = 1;

    @BindView(R.id.user_icon)
    ImageView icon;
    @BindView(R.id.user_name)
    TextView name;
    @BindView(R.id.input_name)
    TextView fullName;
    @BindView(R.id.input_email)
    TextView emailView;
    @BindView(R.id.input_birthday)
    EditText birthDay;
    @BindView(R.id.gender_male)
    RadioButton maleRadio;
    @BindView(R.id.gender_female)
    RadioButton femaleRadio;
    @BindView(R.id.email_layout)
    TextInputLayout emailLayout;
    @BindView(R.id.birthday_layout)
    TextInputLayout birthLayout;

    private Uri chosenImage;
    private long userBirthDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        SharedPreferences preferences = getActivity().getSharedPreferences(USER_INFO_PREFERENCES, Context.MODE_PRIVATE);
        if (preferences.getLong(USER_BIRTHDAY, 0) != 0) {
            birthLayout.setEnabled(false);
        }

        setUpUser();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    chosenImage = data.getData();
                    Log.d(TAG, chosenImage + "");
                    icon.setImageURI(data.getData());
                }
                break;
        }
    }


    @OnClick(R.id.user_icon)
    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.fragment_profile_change_image_intent_dialog)), PICK_IMAGE);
    }

    @OnClick(R.id.input_birthday)
    public void openDateDialog(final EditText editText) {
        Calendar currentDate = Calendar.getInstance();
        int mYear = currentDate.get(Calendar.YEAR);
        int mMonth = currentDate.get(Calendar.MONTH);
        int mDay = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d(TAG, Helper.getUnixStamp(year, month, dayOfMonth) + "");
                userBirthDate = Helper.getUnixStamp(year, month, dayOfMonth);

                editText.setText(Helper.getTime(userBirthDate));
                Log.d(TAG, Helper.getTime(userBirthDate));
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle(getString(R.string.fragment_profile_select_date_dialog));
        mDatePicker.show();
    }

    private void setUpUser() {
        User currentUser = User.getCurrentUser(getActivity());
        name.setText(currentUser.getName());
        fullName.setText(currentUser.getName());
        emailView.setText(currentUser.getEmail());

        userBirthDate = currentUser.getBirthDay();

        birthDay.setText(userBirthDate == 0 ? "" : Helper.getTime(userBirthDate));
        Helper.loadImage(getActivity(), currentUser.getPhotoURL(), icon);
        setGender(currentUser);
    }

    @OnClick(R.id.floatingActionButton)
    public void save() {
        String email = emailView.getText().toString();

        if (!Helper.isValidEmail(email)) {
            emailLayout.setError(getString(R.string.fragment_profile_invalid_email));
            return;
        }

        Intent i = new Intent(getActivity(), SaveProfileService.class);
        i.setData(chosenImage);
        i.putExtra(USER_NAME, fullName.getText().toString());
        i.putExtra(USER_EMAIL, email);
        i.putExtra(USER_GENDER, getGender());
        i.putExtra(USER_BIRTHDAY, userBirthDate);

        getActivity().startService(i);
        EventBus.getDefault().post(new ProfileFinishEvent());
    }

    private String getGender() {
        return maleRadio.isChecked() ? "male" : "female";
    }

    private void setGender(User user) {
        if (user.getGender().isEmpty()) return;
        if (user.getGender().equals("male")) {
            maleRadio.setChecked(true);
        } else {
            femaleRadio.setChecked(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().post(new ProfileFinishEvent());
    }
}
