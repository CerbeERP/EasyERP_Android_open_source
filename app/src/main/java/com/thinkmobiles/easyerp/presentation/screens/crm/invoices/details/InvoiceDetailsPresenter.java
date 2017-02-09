package com.thinkmobiles.easyerp.presentation.screens.crm.invoices.details;

import com.thinkmobiles.easyerp.R;
import com.thinkmobiles.easyerp.data.model.crm.invoice.detail.InvoicePayment;
import com.thinkmobiles.easyerp.data.model.crm.invoice.detail.ResponseGetInvoiceDetails;
import com.thinkmobiles.easyerp.data.model.crm.order.detail.OrderProduct;
import com.thinkmobiles.easyerp.data.model.user.organization.OrganizationSettings;
import com.thinkmobiles.easyerp.presentation.EasyErpApplication;
import com.thinkmobiles.easyerp.presentation.base.rules.ErrorViewHelper;
import com.thinkmobiles.easyerp.presentation.holders.data.crm.HistoryDH;
import com.thinkmobiles.easyerp.presentation.holders.data.crm.InvoicePaymentDH;
import com.thinkmobiles.easyerp.presentation.holders.data.crm.ProductDH;
import com.thinkmobiles.easyerp.presentation.managers.DateManager;
import com.thinkmobiles.easyerp.presentation.screens.crm.dashboard.detail.charts.DollarFormatter;
import com.thinkmobiles.easyerp.presentation.utils.StringUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

import rx.subscriptions.CompositeSubscription;

/**
 * @author Alex Michenko (Created on 06.02.17).
 *         Company: Thinkmobiles
 *         Email: alex.michenko@thinkmobiles.com
 */

public class InvoiceDetailsPresenter implements InvoiceDetailsContract.InvoiceDetailsPresenter {


    private InvoiceDetailsContract.InvoiceDetailsView view;
    private InvoiceDetailsContract.InvoiceDetailsModel model;
    private String invoiceId;
    private CompositeSubscription compositeSubscription;

    private ResponseGetInvoiceDetails currentInvoice;
    private OrganizationSettings organizationSettings;
    private boolean isVisibleHistory;
    private String notSpecified;
    private DecimalFormat formatter;


    public InvoiceDetailsPresenter(InvoiceDetailsContract.InvoiceDetailsView view, InvoiceDetailsContract.InvoiceDetailsModel model, String invoiceId) {
        this.view = view;
        this.model = model;
        this.invoiceId = invoiceId;
        view.setPresenter(this);

        compositeSubscription = new CompositeSubscription();
        notSpecified = EasyErpApplication.getInstance().getString(R.string.err_not_specified);
        formatter = new DollarFormatter().getFormat();
    }

    @Override
    public void changeNotesVisibility() {
        isVisibleHistory = !isVisibleHistory;
        view.showHistory(isVisibleHistory);
    }

    @Override
    public void refresh() {
        compositeSubscription.add(model.getInvoiceDetails(invoiceId)
                .subscribe(responseGerOrderDetails -> {
                    view.showProgress(false);
                    setData(responseGerOrderDetails);
                }, t -> {
                    view.showProgress(false);
                    t.printStackTrace();
                    if (currentInvoice == null) {
                        view.showError(t.getMessage(), ErrorViewHelper.ErrorType.NETWORK);
                    } else {
                        view.showMessage(t.getMessage());
                    }
                }));
    }

    private void getOrganizationSettings() {
        compositeSubscription.add(model.getOrganizationSettings()
                .subscribe(responseGetOrganizationSettings -> {
                    organizationSettings = responseGetOrganizationSettings.data;

                }, Throwable::printStackTrace));
    }

    @Override
    public void subscribe() {
        if (currentInvoice == null) {
            view.showProgress(true);
            refresh();
            getOrganizationSettings();
        } else {
            setData(currentInvoice);
        }
    }

    private void setData(ResponseGetInvoiceDetails response) {
        currentInvoice = response;

        view.setInvoiceStatusName(response.workflow.name);
        view.setInvoiceStatus(response.workflow.status);
        view.setInvoiceName(response.name);
        view.setInvoiceDate(DateManager.convert(response.invoiceDate).setDstPattern(DateManager.PATTERN_DATE_SIMPLE_PREVIEW).toString());
        if (response.dueDate != null) {
            view.setDueDate(DateManager.convert(response.dueDate).setDstPattern(DateManager.PATTERN_DATE_SIMPLE_PREVIEW).toString());
        }
        view.setOrderNumber(response.paymentReference);
        view.setSupplierName(response.supplier.fullName);
        String symbol = response.currency.id != null ? response.currency.id.symbol : "$";
        if (response.paymentInfo != null) {
            view.setSubTotal(StringUtil.getFormattedPriceFromCent(formatter, response.paymentInfo.unTaxed, symbol));
            if (response.paymentInfo.discount > 0)
                view.setDiscount(StringUtil.getFormattedPriceFromCent(formatter, - response.paymentInfo.discount, symbol));
            view.setTaxes(StringUtil.getFormattedPriceFromCent(formatter, response.paymentInfo.taxes, symbol));
            view.setTotal(StringUtil.getFormattedPriceFromCent(formatter, response.paymentInfo.total, symbol));
            Double paymentMade = response.paymentInfo.balance - response.paymentInfo.total;
            if (paymentMade != 0) {
                view.setPaymentMade(StringUtil.getFormattedPriceFromCent(formatter, paymentMade, symbol));
            }
            view.setBalanceDue(StringUtil.getFormattedPriceFromCent(formatter, response.paymentInfo.balance, symbol));
        }
        if (organizationSettings != null) {
            view.setCompanyName(organizationSettings.name);
            if (organizationSettings.address != null)
                view.setCompanyAddress(StringUtil.getAddress(organizationSettings.address));
        }

        if (response.attachments != null && !response.attachments.isEmpty()) {
            view.setAttachments(StringUtil.getAttachments(response.attachments));
        }

        if (response.payments != null && !response.payments.isEmpty()) {
            view.setPayments(preparePaymentsList(response.payments));
        }

        if (response.products != null) {
            view.setProducts(prepareProductList(response.products, symbol));
        }
        view.setHistory(HistoryDH.convert(response.notes));

        view.showHistory(isVisibleHistory);
    }

    private ArrayList<ProductDH> prepareProductList(ArrayList<OrderProduct> products, String symbol) {
        ArrayList<ProductDH> list = new ArrayList<>();
        for (OrderProduct product : products){
            list.add(new ProductDH(product, symbol));
        }
        return list;
    }

    private ArrayList<InvoicePaymentDH> preparePaymentsList(ArrayList<InvoicePayment> products) {
        ArrayList<InvoicePaymentDH> list = new ArrayList<>();
        for (InvoicePayment product : products){
            list.add(new InvoicePaymentDH(product));
        }
        return list;
    }

    @Override
    public void unsubscribe() {
        compositeSubscription.clear();
    }
}
