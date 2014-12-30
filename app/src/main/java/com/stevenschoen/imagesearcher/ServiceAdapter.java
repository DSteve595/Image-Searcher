package com.stevenschoen.imagesearcher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceHolder> {
    private static SearchInterface.Service[] services = new SearchInterface.Service[]{
            SearchInterface.Service.Google, SearchInterface.Service.Bing
    };

    @Override
    public ServiceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_grid, parent, false);

        ServiceHolder holder = new ServiceHolder(view);
        holder.root = view;
        holder.name = (TextView) view.findViewById(R.id.service_grid_name);
        holder.priceHolder = view.findViewById(R.id.service_grid_price_holder);
        holder.price = (TextView) view.findViewById(R.id.service_grid_price);
        holder.priceInfo = view.findViewById(R.id.service_grid_price_info);

        return holder;
    }

    @Override
    public void onBindViewHolder(ServiceHolder holder, int position) {
        SearchInterface.Service service = services[position];

        String name;
        boolean paid;
        String price = null;

        switch (service) {
            case Google:
                name = "Google";
                paid = true;
                price = "$1.99";
                break;
            case Bing:
                name = "Bing";
                paid = false;
                break;
            default:
                name = "Unknown";
                paid = false;
        }

        holder.name.setText(name);
        if (paid) {
            holder.priceHolder.setVisibility(View.VISIBLE);
//            holder.priceHolder // buy
            holder.price.setText(price);
        } else {
            holder.priceHolder.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return services.length;
    }

    public static class ServiceHolder extends RecyclerView.ViewHolder {
        public View root;
        public TextView name;
        public View priceHolder;
        public TextView price;
        public View priceInfo;

        public ServiceHolder(View itemView) {
            super(itemView);
        }
    }
}
