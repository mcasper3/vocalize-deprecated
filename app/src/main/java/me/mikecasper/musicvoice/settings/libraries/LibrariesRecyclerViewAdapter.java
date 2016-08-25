package me.mikecasper.musicvoice.settings.libraries;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.mikecasper.musicvoice.R;

public class LibrariesRecyclerViewAdapter extends RecyclerView.Adapter<LibrariesRecyclerViewAdapter.ViewHolder> {

    private String[] mLibraries;
    private String[] mLicenses;

    public LibrariesRecyclerViewAdapter(Context context) {
        mLibraries = context.getResources().getStringArray(R.array.libraries);
        mLicenses = context.getResources().getStringArray(R.array.licenses);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_license, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // TODO bind correct view (need to just have list of names I think as first item name\nname2\n etc.);

        holder.mLibraryName.setText(mLibraries[position]);
        holder.mLicense.setText(mLicenses[position]);
    }

    @Override
    public int getItemCount() {
        return mLibraries.length + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mLibraryName;
        public final TextView mLicense;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLibraryName = (TextView) view.findViewById(R.id.library_name);
            mLicense = (TextView) view.findViewById(R.id.content);
        }
    }
}
