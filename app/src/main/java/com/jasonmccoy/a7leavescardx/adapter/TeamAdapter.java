package com.jasonmccoy.a7leavescardx.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jasonmccoy.a7leavescardx.Helper;
import com.jasonmccoy.a7leavescardx.R;
import com.jasonmccoy.a7leavescardx.items.TeamMember;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamMemberViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private final ArrayList<Map.Entry<String, TeamMember>> members = new ArrayList<>();

    public TeamAdapter(Context context, Map<String, TeamMember> map) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.members.addAll(map.entrySet());
    }

    @Override
    public TeamMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.team_item_layout, parent, false);
        return new TeamMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TeamMemberViewHolder holder, int position) {
        Map.Entry<String, TeamMember> entry = getItemAt(position);
        TeamMember member = entry.getValue();

        holder.userName.setText(member.getName());
        holder.userCount.setText(member.getStampCount() > 7 ?
                String.format(Locale.ENGLISH, context.getString(R.string.team_count_info), member.getStampCount()) : "");

        holder.redeemMessage.setVisibility(member.getRedeemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        Helper.loadImage(context, member.getPhotoURL(), holder.userIcon);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateTeamMember(String key, TeamMember newValue) {
        int itemThatChanged = -1;
        for (int i = 0, size = members.size(); i < size; i++) {
            Map.Entry<String, TeamMember> memberEntry = getItemAt(i);
            if (memberEntry.getKey().equals(key)) {
                TeamMember member = memberEntry.getValue();
                int changes = TeamMember.updateMember(member, newValue);
                if (changes > 0) itemThatChanged = i;
            }
        }

        if (itemThatChanged != -1) notifyItemChanged(itemThatChanged);
    }


    private Map.Entry<String, TeamMember> getItemAt(int position) {
        return members.get(position);
    }

    class TeamMemberViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_icon)
        ImageView userIcon;

        @BindView(R.id.user_name)
        TextView userName;

        @BindView(R.id.user_count)
        TextView userCount;

        @BindView(R.id.redeem_message)
        TextView redeemMessage;

        TeamMemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
