package de.gymnasium_beetzendorf.vertretungsplan;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.Substitution;

/**
 * Created by davidfrenzel on 26.04.17.
 */

public class SubstitutionDiffUtil extends DiffUtil.Callback {

    private List<Substitution> oldSubstitutionList;
    private List<Substitution> newSubstitutionList;


    public SubstitutionDiffUtil(List<Substitution> oldSubstitutionList, List<Substitution> newSubstitutionList) {
        this.oldSubstitutionList = oldSubstitutionList;
        this.newSubstitutionList = newSubstitutionList;
    }

    @Override
    public int getOldListSize() {
        return oldSubstitutionList.size();
    }

    @Override
    public int getNewListSize() {
        return newSubstitutionList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldSubstitutionList.get(oldItemPosition).equals(newSubstitutionList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldSubstitutionList.get(oldItemPosition).equals(newSubstitutionList.get(newItemPosition));
    }
}
