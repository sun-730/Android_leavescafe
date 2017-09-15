package com.jasonmccoy.a7leavescardx.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jasonmccoy.a7leavescardx.R;
import com.jasonmccoy.a7leavescardx.events.FeedbackFinishEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class FeedbackFragment extends Fragment {
    public FeedbackFragment() {
    }

    public static final String TAG = TEST + FeedbackFragment.class.getSimpleName();


    @BindView(R.id.layout_message)
    TextInputLayout messageLayout;
    @BindView(R.id.input_message)
    EditText messageView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        ButterKnife.bind(this, view);


        return view;
    }

    @OnClick(R.id.fab)
    public void sendFeedback() {
        String message = messageView.getText().toString().trim();
        if (message.isEmpty()) {
            messageLayout.setError(getString(R.string.fragment_feedback_empty_message));
            return;
        }

        ShareCompat.IntentBuilder.from(getActivity())
                .setType("message/rfc822")
                .addEmailTo(getString(R.string.fragment_feedback_destination_email))
                .setSubject(getString(R.string.app_name) + getString(R.string.fragment_feedback_subject))
                .setText(message)
                .setChooserTitle(getString(R.string.fragment_feedback_intent_title))
                .startChooser();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().post(new FeedbackFinishEvent());
    }
}
