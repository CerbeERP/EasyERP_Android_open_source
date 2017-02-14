package com.thinkmobiles.easyerp.presentation.utils.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FilterQuery {

    final public Map<String, String> queryMap;
    public ArrayList<String> contactName;
    public ArrayList<String> assignedTo;
    public ArrayList<String> source;
    public ArrayList<String> createdBy;
    public ArrayList<String> workflow;
    public ArrayList<String> customer;
    public ArrayList<String> name;

    public FilterQuery() {
        this.queryMap = new HashMap<>();
    }

    public static class Builder {

        private final FilterTypeQuery contactName;
        private final FilterTypeQuery createdBy;
        private final FilterTypeQuery assignedTo;
        private final FilterTypeQuery source;
        private final FilterTypeQuery workflow;
        private final FilterTypeQuery customer;
        private final FilterTypeQuery name;

        public Builder() {
            contactName = new FilterTypeQuery("contactName", "contactName");
            createdBy = new FilterTypeQuery("createdBy", "createdBy.user._id");
            assignedTo = new FilterTypeQuery("salesPerson", "salesPerson._id");
            source = new FilterTypeQuery("source", "source");
            workflow = new FilterTypeQuery("workflow", "workflow._id");
            customer = new FilterTypeQuery("customer", "customer");
            name = new FilterTypeQuery("name", "_id");
        }



        public FilterTypeQuery forContactName() {
            return contactName;
        }

        public FilterTypeQuery forCreatedBy() {
            return createdBy;
        }

        public FilterTypeQuery forAssignedTo() {
            return assignedTo;
        }

        public FilterTypeQuery forSource() {
            return source;
        }

        public FilterTypeQuery forWorkflow() {
            return workflow;
        }

        public FilterTypeQuery forCustomer() {
            return customer;
        }

        public FilterTypeQuery forName() {
            return name;
        }

        public FilterQuery build() {
            FilterQuery query = new FilterQuery();

            query.contactName = contactName.save(query.queryMap);
            query.assignedTo = assignedTo.save(query.queryMap);
            query.createdBy = createdBy.save(query.queryMap);
            query.source = source.save(query.queryMap);
            query.workflow = workflow.save(query.queryMap);
            query.name = name.save(query.queryMap);
            query.customer = customer.save(query.queryMap);

            return query;
        }
    }

}
