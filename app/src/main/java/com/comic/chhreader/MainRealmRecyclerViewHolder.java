package com.comic.chhreader;

import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.comic.chhreader.detail.DetailActivity;
import com.comic.chhreader.model.Post;
import io.realm.RealmViewHolder;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class MainRealmRecyclerViewHolder extends RealmViewHolder {
    private ImageView thumbnail;
    private TextView title;
    private TextView content;
    private View section;
    private TextView sectionText;

    public MainRealmRecyclerViewHolder(View itemView) {
        super(itemView);
        section = itemView.findViewById(R.id.list_section);
        sectionText = (TextView) itemView.findViewById(R.id.list_section_text);
        thumbnail = (ImageView) itemView.findViewById(R.id.content_ori_image);
        title = (TextView) itemView.findViewById(R.id.content_title_text);
        content = (TextView) itemView.findViewById(R.id.content_subcontent_text);
    }

    public static boolean isSameDate(long date1, long date2) {
        long days1 = date1 / (60 * 60 * 24);
        long days2 = date2 / (60 * 60 * 24);
        return days1 == days2;
    }

    public void bindItem(Post post, Post prePost) {
        if (post == null || !post.isValid()) {
            return;
        }
        if (section != null && sectionText != null) {
            if (prePost == null || !prePost.isValid()) {
                section.setVisibility(View.VISIBLE);
                sectionText.setText("最新");
            } else {
                long prevDate = 0;
                prevDate = Long.parseLong(prePost.getPostdate());
                long date = Long.parseLong(post.getPostdate());
                if (isSameDate(prevDate, date)) {
                    section.setVisibility(View.GONE);
                } else {
                    section.setVisibility(View.VISIBLE);
                    sectionText.setText(DateUtils.formatDateTime(itemView.getContext(), date * 1000,
                                                                 DateUtils.FORMAT_SHOW_DATE));
                }
            }
        }

        if (thumbnail != null) {
            if (!TextUtils.isEmpty(post.getImageUrl())) {
                Glide.with(itemView.getContext().getApplicationContext())
                    .load(post.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.default_image)
                    .crossFade()
                    .into(thumbnail);
            } else {
                thumbnail.setImageResource(R.drawable.default_image);
            }
        }
        if (title != null) {
            title.setText(post.getName());
        }
        if (content != null) {
            String contentText = post.getContent();
            content.setText(Html.fromHtml(contentText));
        }
        String name = post.getName();
        String url = post.getLink();
        Long id = post.getId();
        itemView.setOnClickListener(view -> {
            Intent intent = new Intent(itemView.getContext(), DetailActivity.class);
            intent.putExtra("title", name);
            intent.putExtra("url", url);
            intent.putExtra("id", id);
            itemView.getContext().startActivity(intent);
        });
    }
}
