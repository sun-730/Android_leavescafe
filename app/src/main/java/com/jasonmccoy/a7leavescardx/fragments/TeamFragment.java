package com.jasonmccoy.a7leavescardx.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jasonmccoy.a7leavescardx.Helper;
import com.jasonmccoy.a7leavescardx.R;
import com.jasonmccoy.a7leavescardx.adapter.TeamAdapter;
import com.jasonmccoy.a7leavescardx.events.TeamFinishEvent;
import com.jasonmccoy.a7leavescardx.items.TeamMember;
import com.jasonmccoy.a7leavescardx.items.User;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jasonmccoy.a7leavescardx.AppClass.DATABASE_NODE_USER_TEAMS;
import static com.jasonmccoy.a7leavescardx.AppClass.TEST;

public class TeamFragment extends Fragment {
    public TeamFragment() {
    }

    public static final String TAG = TEST + TeamFragment.class.getSimpleName();
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.empty_team)
    TextView emptyTeam;

    private TeamAdapter adapter;
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        ButterKnife.bind(this, view);

        currentUser = User.getCurrentUser(getActivity());

        DatabaseReference userTeamRef = Helper.getUserReference(getActivity()).child(DATABASE_NODE_USER_TEAMS);
        userTeamRef.addListenerForSingleValueEvent(currentUserListener);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().post(new TeamFinishEvent());
    }

    ValueEventListener currentUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) return;

            HashMap<String, TeamMember> map = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, TeamMember>>() {
            });

            updatedTeamMates(map);

            for (String memberKey : map.keySet())
                Helper.getUserReferenceByKey(memberKey).addValueEventListener(teamMatesListener);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener teamMatesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (adapter == null) return;

            String key = dataSnapshot.getKey();
            User user = dataSnapshot.getValue(User.class);

            if (user == null) return;

            Log.d(TAG, user.toString());
            TeamMember teamMember = TeamMember.getTeamMember(user);

            adapter.updateTeamMember(key, teamMember);

            Helper.getUserReferenceByKey(currentUser.getKey())
                    .child(DATABASE_NODE_USER_TEAMS)
                    .child(key)
                    .setValue(teamMember);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };


    private void updatedTeamMates(Map<String, TeamMember> map) {
        emptyTeam.setVisibility(View.GONE);

        adapter = new TeamAdapter(getActivity(), map);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }
}
