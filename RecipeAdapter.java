package com.example.grannystable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends BaseAdapter {
    private Context context;
    private List<Object> displayList; // Can be Recipe or Subcategory String

    public RecipeAdapter(Context context, List<Object> displayList) {
        this.context = context;
        this.displayList = displayList != null ? displayList : new ArrayList<>();
    }

    @Override
    public int getCount() {
        return displayList.size();
    }

    @Override
    public Object getItem(int position) {
        return displayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position) instanceof Recipe ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object item = getItem(position);

        if (item instanceof String) {
            // Subcategory Header
            TextView subcategoryTextView;
            if (convertView == null || !(convertView.getTag() instanceof TextView)) {
                convertView = LayoutInflater.from(context).inflate(R.layout.subcategory_header, parent, false);
                subcategoryTextView = convertView.findViewById(R.id.subcategoryTitle);
                convertView.setTag(subcategoryTextView);
            } else {
                subcategoryTextView = (TextView) convertView.getTag();
            }
            subcategoryTextView.setText((String) item);
        } else {
            // Recipe Item
            RecipeViewHolder holder;
            if (convertView == null || !(convertView.getTag() instanceof RecipeViewHolder)) {
                convertView = LayoutInflater.from(context).inflate(R.layout.recipe_list_item, parent, false);
                holder = new RecipeViewHolder();
                holder.titleTextView = convertView.findViewById(R.id.recipeTitle);
                holder.imageView = convertView.findViewById(R.id.recipeImage);
                convertView.setTag(holder);
            } else {
                holder = (RecipeViewHolder) convertView.getTag();
            }

            Recipe recipe = (Recipe) item;
            holder.titleTextView.setText(recipe.getTitle());

            Picasso.get()
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageView);
        }
        return convertView;
    }



    static class RecipeViewHolder {
        TextView titleTextView;
        ImageView imageView;
    }

    public void updateList(List<Object> newList) {
        if (newList != null) {
            displayList = new ArrayList<>(newList); // Instead of modifying original list, create a new one
            notifyDataSetChanged();
        }
    }
    @Override
    public int getViewTypeCount() {
        return 2; // One for subcategory headers, one for recipes
    }


}
