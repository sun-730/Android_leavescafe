package com.jasonmccoy.a7leavescardx.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jasonmccoy.a7leavescardx.R;
import com.jasonmccoy.a7leavescardx.events.ReferFinishEvent;
import com.jasonmccoy.a7leavescardx.items.User;
import com.pddstudio.urlshortener.URLShortener;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class ReferFragment extends Fragment {
    public ReferFragment() {
    }

    public static final String TAG = TEST + ReferFragment.class.getSimpleName();

    @BindView(R.id.refer_code)
    TextView userRefCode;

    private User user;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refer, container, false);
        ButterKnife.bind(this, view);
        progressDialog = new ProgressDialog(getActivity());
        user = User.getCurrentUser(getActivity());
        userRefCode.setText(user.getReferralCode());

        return view;
    }

    @OnClick(R.id.refer_button)
    public void sendInvite() {
        progressDialog.show();

        if (user.getReferralCode().isEmpty()) {
            Toast.makeText(getActivity(), R.string.fragment_refer_loading_code, Toast.LENGTH_LONG).show();
        }

        String userLink = "http://7leavescafe.com/app_share_code?redeem_code=" + user.getReferralCode();


        String linkStart = getString(R.string.dynamic_link_start);
        String linkMiddle = null;
        try {
            linkMiddle = URLEncoder.encode(userLink, "UTF-8");
            Log.d(TAG, linkMiddle);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String linkEnd = getString(R.string.dynamic_link_end);

        final String[] fullLink = {linkStart + linkMiddle + linkEnd};

        URLShortener.shortUrl(fullLink[0], new URLShortener.LoadingCallback() {
            @Override
            public void startedLoading() {

            }

            @Override
            public void finishedLoading(@Nullable String shortUrl) {
                if (shortUrl != null) {
                    fullLink[0] = shortUrl;
                }

                Intent sendIntent = new Intent();
                String msg = getString(R.string.fragment_refer_redeem_message, user.getReferralCode()) + "\n" + fullLink[0];
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().post(new ReferFinishEvent());
    }
}
