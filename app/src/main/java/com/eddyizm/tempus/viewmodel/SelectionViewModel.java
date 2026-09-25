package com.eddyizm.tempus.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Activity-scoped selection state, shared between whichever list fragment is showing
 * songs (SongListPageFragment today, others later) and SongBottomSheetDialog, which
 * triggers "select" mode from its own destination.
 *
 * Because it's obtained via new ViewModelProvider(requireActivity()), the same instance
 * is handed to both, without either needing a direct reference to the other.
 */
public class SelectionViewModel extends ViewModel {

    private final MutableLiveData<Boolean> selectionModeActive = new MutableLiveData<>(false);
    private final MutableLiveData<LinkedHashSet<String>> selectedIds = new MutableLiveData<>(new LinkedHashSet<>());

    public LiveData<Boolean> getSelectionModeActive() {
        return selectionModeActive;
    }

    public LiveData<LinkedHashSet<String>> getSelectedIds() {
        return selectedIds;
    }

    /** Called from SongBottomSheetDialog's new "Select" row. */
    public void startSelection(String firstSongId) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (firstSongId != null) {
            ids.add(firstSongId);
        }
        selectedIds.setValue(ids);
        selectionModeActive.setValue(true);
    }

    /** Called when the user taps a song while selection mode is active. */
    public void toggle(String songId) {
        if (songId == null) return;
        if (Boolean.TRUE.equals(selectionModeActive.getValue())) {
            LinkedHashSet<String> current = selectedIds.getValue();
            LinkedHashSet<String> updated = current != null ? new LinkedHashSet<>(current) : new LinkedHashSet<>();

            if (!updated.remove(songId)) {
                updated.add(songId);
            }

            selectedIds.setValue(updated);

            // Deselecting the last item exits selection mode automatically.
            if (updated.isEmpty()) {
                selectionModeActive.setValue(false);
            }
        }
    }

    public void clearSelection() {
        selectionModeActive.setValue(false);
        selectedIds.setValue(new LinkedHashSet<>());
    }

    /** Selects every id given (typically every song currently visible in the list). */
    public void selectAll(java.util.Collection<String> ids) {
        if (ids == null) return;
        selectedIds.setValue(new LinkedHashSet<>(ids));
        selectionModeActive.setValue(true);
    }

    /**
     * Empties the selection but, unlike clearSelection/Cancel, leaves selection mode active — this
     * is a deliberate "start over" action, not the same as unchecking the last box (which exits
     * selection mode automatically in toggle() above).
     */
    public void deselectAll() {
        selectedIds.setValue(new LinkedHashSet<>());
    }

    public Set<String> currentSelection() {
        LinkedHashSet<String> ids = selectedIds.getValue();
        return ids != null ? ids : new LinkedHashSet<>();
    }
}