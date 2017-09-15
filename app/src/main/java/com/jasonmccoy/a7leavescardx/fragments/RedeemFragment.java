package com.jasonmccoy.a7leavescardx.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jasonmccoy.a7leavescardx.Helper;
import com.jasonmccoy.a7leavescardx.R;
import com.jasonmccoy.a7leavescardx.events.RedeemFinishEvent;
import com.jasonmccoy.a7leavescardx.items.TeamMember;
import com.jasonmccoy.a7leavescardx.items.User;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_STAMP_COUNT;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;


public class RedeemFragment extends Fragment {
    public RedeemFragment() {
    }

    public static final String TAG = TEST + RedeemFragment.class.getSimpleName();
    public static final String ARGS_REFERRAL_CODE = "ARGS_REFERRAL_CODE";

    @BindView(R.id.input_code)
    EditText codeInput;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.code_layout)
    TextInputLayout codeLayout;

    private String userWhoReferredKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_redeem, container, false);
        ButterKnife.bind(this, view);

        String referralCode = getArguments().getString(ARGS_REFERRAL_CODE, null);
        if (referralCode != null) codeInput.setText(referralCode);

        return view;
    }

    @OnClick(R.id.verify_button)
    public void verifyCode() {
        progressBar.setVisibility(View.VISIBLE);
        String code = codeInput.getText().toString();

        if (code.equals(User.getCurrentUser(getActivity()).getReferralCode())) {
            codeLayout.setError(getString(R.string.fragment_redeem_error_own_code));
            return;
        }

        Helper.queryUserByReferralCode(code.trim()).addListenerForSingleValueEvent(codeQuery);
    }

    ValueEventListener codeQuery = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getChildrenCount() == 0) {
                codeLayout.setError(getString(R.string.fragment_redeem_invalid_code));
                progressBar.setVisibility(View.GONE);
                return;
            }

            User thisUser = User.getCurrentUser(getActivity());
            HashMap<String, User> map = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, User>>() {
            });

            userWhoReferredKey = map.keySet().iterator().next();
            User otherUser = map.get(userWhoReferredKey);
            ArrayList<TeamMember> teamMembers = otherUser.getTeamArray();

            if (teamMembers.isEmpty()) {
                DatabaseReference thisUserRef = Helper.getUserReference(getActivity());
                thisUserRef.addListenerForSingleValueEvent(updateThisUserStamps);
                return;
            }


            for (TeamMember member : teamMembers) {
                if (member.getName().equals(thisUser.getName())
                        && member.getPhotoURL().equals(thisUser.getPhotoURL())) {
                    codeLayout.setError(getString(R.string.fragment_redeem_code_used));
                    progressBar.setVisibility(View.GONE);
                    return;
                }
            }

            DatabaseReference thisUserRef = Helper.getUserReference(getActivity());
            thisUserRef.addListenerForSingleValueEvent(updateThisUserStamps);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    ValueEventListener updateThisUserStamps = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);

            Helper.getUserReference(getActivity())
                    .child(DATABASE_NODE_USER_STAMP_COUNT)
                    .setValue(user.getStampCount() + 1);

            Helper.addUserToTeam(userWhoReferredKey, user.getKey(), TeamMember.getTeamMemberWithUpdateStamps(user));
            progressBar.setVisibility(View.GONE);
            EventBus.getDefault().post(new RedeemFinishEvent());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };


    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().post(new RedeemFinishEvent());
    }
}
