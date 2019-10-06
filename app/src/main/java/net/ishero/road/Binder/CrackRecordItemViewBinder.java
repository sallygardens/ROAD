package net.ishero.road.Binder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.ishero.road.Item.CrackRecordItem;
import net.ishero.road.R;
import net.ishero.road.activity.CrackDetailActivity;

import me.drakeet.multitype.ItemViewBinder;

public class CrackRecordItemViewBinder extends ItemViewBinder<CrackRecordItem, CrackRecordItemViewBinder.ViewHolder> {
    private View root;
    private TextView tvRoadName;
    private TextView tvDist;

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        root = inflater.inflate(R.layout.item_crack_record_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull CrackRecordItem crackRecordItem) {
        initView();
    }

    private void initView() {
        tvRoadName = root.findViewById(R.id.tv_item_name);
        tvDist = root.findViewById(R.id.tv_item_distance);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.getContext().startActivity(new Intent(root.getContext(), CrackDetailActivity.class));
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
