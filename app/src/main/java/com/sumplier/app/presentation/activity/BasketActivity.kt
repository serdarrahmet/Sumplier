package com.sumplier.app.presentation.activity

import BasketDetailFragment
import WarningPopup
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sumplier.app.R
import com.sumplier.app.app.Config
import com.sumplier.app.data.model.Category
import com.sumplier.app.data.model.CompanyAccount
import com.sumplier.app.data.model.Product
import com.sumplier.app.data.model.TicketOrder
import com.sumplier.app.presentation.adapter.CategoryAdapter
import com.sumplier.app.presentation.adapter.ProductAdapter


class BasketActivity : AppCompatActivity() {

    private lateinit var currentAccount: CompanyAccount
    private lateinit var menuCategories: List<Category>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var totalPriceTv: TextView
    private lateinit var totalQuantityTv: TextView


    private var totalPrice : Double = 0.0

    private var currentItems: ArrayList<TicketOrder> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basket)

        val accountId = intent.getLongExtra("account_id", -1L)
        if (accountId == -1L) {
            Toast.makeText(this, "Geçersiz Account: $accountId", Toast.LENGTH_SHORT).show()
            return
        }

        currentAccount = Config.getInstance().getAccountById(accountId) ?: return
        menuCategories = Config.getInstance().getMenuCategory(Config.getInstance().getDefaultMenu())

        totalPriceTv = findViewById(R.id.totalPriceValue)
        totalQuantityTv = findViewById(R.id.quantityText)

        findViewById<LinearLayout>(R.id.onSendBasket)?.setOnClickListener {

            openBasketDetailFragment()
        }


        setupRecyclerView()
        updatePriceAndQuantity()
    }

    private fun setupCategoryRecyclerView() {
        val recyclerViewCategories = findViewById<RecyclerView>(R.id.recyclerViewCategories)

        categoryAdapter = CategoryAdapter(menuCategories) { category ->
            updateProductList(category)
        }

        recyclerViewCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCategories.adapter = categoryAdapter
    }

    private fun updateProductList(category: Category) {
        val productsForCategory = getProductsForCategory(category)
        productAdapter.updateProducts(productsForCategory)
    }

    private fun updatePriceAndQuantity(){
        totalPriceTv.text = totalPrice.toString()
        totalQuantityTv.text = currentItems.size.toString() + " Adet"

    }


    private fun openBasketDetailFragment() {

        if (currentItems.isEmpty()) {

            val warningPopup = WarningPopup(
                warningText = "Sepetinizi görüntülemek için lütfen ürün ekleyiniz",
                warningIcon = R.drawable.cancel_ic,
                yesButtonText = "Tamam",
                noButtonText = "",
                showNoButton = false
            ).apply {
                setOnYesClickListener {
                    dismiss()
                }
            }

            warningPopup.show(supportFragmentManager, "WarningPopup")
            return
        }

        findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.VISIBLE
        
        val basketDetailFragment = BasketDetailFragment().apply {
            setBasketItems(currentItems)
            setCurrentAccount(currentAccount)
            setOnBasketUpdatedListener { updatedItems ->
                currentItems = updatedItems
                updatePriceAndQuantity()
                calculateTotalPrice()
            }
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, basketDetailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun calculateTotalPrice() {
        totalPrice = currentItems.sumOf { it.totalPrice }
        updatePriceAndQuantity()
    }


    private fun setupProductRecyclerView() {
        val recyclerViewProducts = findViewById<RecyclerView>(R.id.recyclerViewProducts)

        productAdapter = ProductAdapter(getProductsForCategory(menuCategories[0])) { product, quantity ->
            // Check product if already exist
            val existingItem = currentItems.find { it.productCode == product.productCode }

            if (existingItem != null) {
                // Update if exist
                existingItem.apply {
                    this.quantity += quantity
                    this.totalPrice = this.price * this.quantity
                }
            } else {
                // Add new item if not exist
                val itemTicketOrder = TicketOrder(
                    id = 0,
                    ticketId = 0,
                    productCode = product.productCode,
                    productName = product.productName,
                    quantity = quantity,
                    price = product.price,
                    totalPrice = product.price * quantity,
                    discountPrice = 0.0,
                    status = 0,
                    isChange = false,
                    newQuantity = 0.0,
                    newPrice = 0.0,
                    newTotalPrice = 0.0,
                    companyCode = Config.getInstance().getCurrentCompany().companyCode,
                    deviceCode = "TEST01"
                )
                currentItems.add(itemTicketOrder)
            }

            calculateTotalPrice()
        }

        recyclerViewProducts.layoutManager = GridLayoutManager(this, 2)
        recyclerViewProducts.adapter = productAdapter
    }

    private fun setupRecyclerView() {
        setupCategoryRecyclerView()
        setupProductRecyclerView()

    }

    private fun getProductsForCategory(category: Category): List<Product> {
        return Config.getInstance().getCategoryProducts(category)
    }
}
