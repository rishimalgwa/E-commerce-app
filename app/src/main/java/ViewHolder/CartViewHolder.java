package ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommercedemo.R;

import Interface.ItemClickListner;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
public TextView textProductName, textProductPrice,textProductQuantity;
private ItemClickListner itemClickListner;
    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        textProductName = itemView.findViewById(R.id.cart_product_name);
        textProductPrice = itemView.findViewById(R.id.cart_product_price);
        textProductQuantity = itemView.findViewById(R.id.cart_product_quantity);
    }

    @Override
    public void onClick(View v) {
        itemClickListner.onClick(v,getAdapterPosition(),false);
    }

    public void setItemClickListner(ItemClickListner itemClickListner) {
        this.itemClickListner = itemClickListner;
    }
}
