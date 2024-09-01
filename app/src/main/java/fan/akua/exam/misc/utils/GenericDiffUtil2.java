package fan.akua.exam.misc.utils;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class GenericDiffUtil2<T> extends DiffUtil.Callback {
    private final List<T> oldList;
    private final List<T> newList;

    public GenericDiffUtil2(List<T> oldList, List<T> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).hashCode() == newList.get(newItemPosition).hashCode();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}