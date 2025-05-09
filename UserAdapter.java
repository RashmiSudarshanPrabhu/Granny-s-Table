package com.example.grannystable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;
import java.util.List;

public class UserAdapter extends BaseAdapter {
    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public User getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
        }

        User user = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.userName);
        TextView phoneTextView = convertView.findViewById(R.id.userPhone);
        TextView emailTextView = convertView.findViewById(R.id.userEmail);
        TextView dateTextView = convertView.findViewById(R.id.userDate);

        nameTextView.setText(user.getUsername());
        phoneTextView.setText(user.getPhoneNumber());
        emailTextView.setText(user.getEmail());
        dateTextView.setText(user.getRegistrationDate());

        return convertView;
    }

    public void updateList(List<User> newList) {
        userList.clear();
        userList.addAll(newList);
        notifyDataSetChanged();
    }
}
