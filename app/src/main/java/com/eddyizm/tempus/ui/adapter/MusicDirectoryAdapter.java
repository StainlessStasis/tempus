package com.eddyizm.tempus.ui.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.eddyizm.tempus.databinding.ItemLibraryMusicDirectoryBinding;
import com.eddyizm.tempus.glide.CustomGlideRequest;
import com.eddyizm.tempus.interfaces.ClickCallback;
import com.eddyizm.tempus.subsonic.models.Child;
import com.eddyizm.tempus.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@UnstableApi
public class MusicDirectoryAdapter extends RecyclerView.Adapter<MusicDirectoryAdapter.ViewHolder> {
    private final ClickCallback click;

    private List<Child> children;

    private boolean selectionMode = false;
    private Set<String> selectedIds = Collections.emptySet();

    public MusicDirectoryAdapter(ClickCallback click) {
        this.click = click;
        this.children = Collections.emptyList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLibraryMusicDirectoryBinding view = ItemLibraryMusicDirectoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Child child = children.get(position);

        holder.item.musicDirectoryTitleTextView.setText(child.getTitle());

        CustomGlideRequest.ResourceType type = child.isDir()
                ? CustomGlideRequest.ResourceType.Directory
                : CustomGlideRequest.ResourceType.Song;

        CustomGlideRequest.Builder
                .from(holder.itemView.getContext(), child.getCoverArtId(), type)
                .build()
                .into(holder.item.musicDirectoryCoverImageView);

        holder.item.musicDirectoryMoreButton.setVisibility(child.isDir() ? View.VISIBLE : View.INVISIBLE);
        holder.item.musicDirectoryPlayButton.setVisibility(child.isDir() ? View.VISIBLE : View.INVISIBLE);

        boolean showCheckbox = selectionMode && !child.isDir();
        holder.item.selectionCheckbox.setVisibility(showCheckbox ? View.VISIBLE : View.GONE);
        holder.item.selectionCheckbox.setChecked(showCheckbox && selectedIds.contains(child.getId()));
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    public void setItems(List<Child> children) {
        if (children != null) {
            List<Child> sorted = new ArrayList<>(children);
            sorted.sort((c1, c2) -> {
                if (c1.isDir() && c2.isDir()) {
                    String t1 = c1.getTitle() != null ? c1.getTitle() : "";
                    String t2 = c2.getTitle() != null ? c2.getTitle() : "";
                    return t1.compareToIgnoreCase(t2);
                } else if (!c1.isDir() && !c2.isDir()) {
                    int t1 = c1.getTrack() != null ? c1.getTrack() : 0;
                    int t2 = c2.getTrack() != null ? c2.getTrack() : 0;
                    return Integer.compare(t1, t2);
                } else {
                    return c1.isDir() ? -1 : 1;
                }
            });
            this.children = sorted;
        } else {
            this.children = Collections.emptyList();
        }
        notifyDataSetChanged();
    }

    /** Pushed in by the observing fragment whenever SelectionViewModel's state changes. */
    public void setSelectionState(boolean active, Set<String> selectedIds) {
        this.selectionMode = active;
        this.selectedIds = selectedIds != null ? selectedIds : Collections.emptySet();
        notifyDataSetChanged();
    }

    /** Resolves selected song ids back to Child objects, for "Add to playlist". Folders are never selectable, so this only ever matches songs. */
    public List<Child> getItemsByIds(Set<String> ids) {
        List<Child> result = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return result;
        for (Child child : children) {
            if (!child.isDir() && ids.contains(child.getId())) {
                result.add(child);
            }
        }
        return result;
    }

    /** Every song id currently visible in this folder (folders themselves excluded), for "Select all". */
    public List<String> getAllVisibleIds() {
        List<String> ids = new ArrayList<>();
        for (Child child : children) {
            if (!child.isDir()) {
                ids.add(child.getId());
            }
        }
        return ids;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemLibraryMusicDirectoryBinding item;

        ViewHolder(ItemLibraryMusicDirectoryBinding item) {
            super(item.getRoot());

            this.item = item;

            item.musicDirectoryTitleTextView.setSelected(true);

            itemView.setOnClickListener(v -> onClick());
            itemView.setOnLongClickListener(v -> onLongClick());

            item.musicDirectoryMoreButton.setOnClickListener(v -> onClick());
            item.musicDirectoryPlayButton.setOnClickListener(v -> onPlayClick());
        }

        public void onClick() {
            Bundle bundle = new Bundle();

            Child tapped = children.get(getBindingAdapterPosition());

            if (tapped.isDir()) {
                bundle.putString(Constants.MUSIC_DIRECTORY_ID, tapped.getId());
                click.onMusicDirectoryClick(bundle);
                return;
            }

            if (selectionMode) {
                bundle.putParcelable(Constants.TRACK_OBJECT, tapped);
                click.onSongSelectionToggle(bundle);
                return;
            }

            bundle.putParcelableArrayList(Constants.TRACKS_OBJECT, new ArrayList<>(children));
            bundle.putInt(Constants.ITEM_POSITION, getBindingAdapterPosition());
            click.onMediaClick(bundle);
        }

        private boolean onLongClick() {
            if (!children.get(getBindingAdapterPosition()).isDir()) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.TRACK_OBJECT, children.get(getBindingAdapterPosition()));

                click.onMediaLongClick(bundle);

                return true;
            } else {
                return false;
            }
        }

        public void onPlayClick() {
            if (children.get(getBindingAdapterPosition()).isDir()) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.MUSIC_DIRECTORY_ID, children.get(getBindingAdapterPosition()).getId());
                click.onMusicDirectoryPlay(bundle);
            }
        }
    }
}