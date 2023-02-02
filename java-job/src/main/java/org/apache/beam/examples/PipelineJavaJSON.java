package org.apache.beam.examples;

import org.apache.avro.JsonProperties;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.schemas.transforms.Join;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.Serializable;
import java.util.HashMap;

import static org.apache.beam.sdk.schemas.transforms.Join.innerJoin;
import static org.apache.beam.sdk.schemas.transforms.Join.leftOuterJoin;


public class PipelineJava {

    // paths to input data
    static String inUsersJSON = "..."
    static String inOrdersJSON = "..."
    static String outMetricResultCountrySum = "..."
    static String outMetricResultOnlineVSBAM = "..."

    static class orderMap extends DoFn<String, Order> {
        @DoFn.ProcessElement
        public void processElement(@Element String element, OutputReceiver<Order> out) throws Exception{
            ObjectMapper mapper = new ObjectMapper();
            Order order = mapper.readValue(element, Order.class);
            out.output(order);
        }

    }

    static class userMap extends DoFn<String, User> {
        @DoFn.ProcessElement
        public void processElement(@Element String element, OutputReceiver<User> out) throws Exception{
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(element, User.class);
            out.output(user);
        }
    }

    static class ShippingInfoBM extends DoFn<Order, Order> {
        @DoFn.ProcessElement
        public void processElement(@Element Order element, OutputReceiver<Order> out) throws Exception{
            if(element.getOrder_type().equals("BRICK AND MORTAR")) {
                element.setShipping_info(null);
            }
            out.output(element);
        }
    }

    static class OnlineOrder extends DoFn<Order, Order> {
        @DoFn.ProcessElement
        public void processElement(@Element Order element, OutputReceiver<Order> out) throws Exception{
            if(element.getOrder_type().equals("ONLINE")){
                if(element.getShipping_info() != null && element.getBilling_info() != null){
                    out.output(element);
                }
            } else if (element.getBilling_info() != null) {
                out.output(element);
            }
        }

    }

    static class orderJoinTransformer extends DoFn<Order, KV<Long, Order>> {
        @DoFn.ProcessElement
        public void processElement(@Element Order element, OutputReceiver<KV<Long, Order>> out) throws Exception{
            KV<Long, Order> keyValue = KV.of(element.getUser_id(), element);
            out.output(keyValue);
        }
    }

    static class userJoinTransformer extends DoFn<User, KV<Long, User>> {
        @DoFn.ProcessElement
        public void processElement(@Element User element, OutputReceiver<KV<Long, User>> out) throws Exception{
            KV<Long, User> keyValue = KV.of(element.getUser_id(), element);
            out.output(keyValue);
        }
    }

    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
        Pipeline p = Pipeline.create(options);

        PCollection<String> usersRaw = p
                .apply(
                        "Read users", TextIO.read().from(inUsersJSON));

        PCollection<User> user = usersRaw.apply(
                "Map users", ParDo.of(new userMap())
        );

        PCollection<String> ordersRaw = p
                .apply(
                "Read orders", TextIO.read().from(inOrdersJSON));

        PCollection<Order> orders = p
                .apply(
                "Map orders", ParDo.of(new orderMap()));

        PCollection<Order> orderCleanOrderType = orders.apply(
            "Filter order type",
                Filter.by(new SerializableFunction<Order, Boolean>(){
                    @Override
                    public Boolean apply(Order input){
                        return  (input.getOrder_type().equals("BRICK AND MORTAR") || input.getOrder_type().equals("ONLINE"));
                    }
                })
        );
        PCollection<Order> orderRemovedShippingInfoForBAM = orderCleanOrderType.apply(
                "Remove shipping info for BAM", ParDo.of(new ShippingInfoBM())
        );
        PCollection<Order> orderRemoveInvalidOnlineOrders = orderRemovedShippingInfoForBAM.apply(
                "Filter online orders", ParDo.of(new OnlineOrder())
        );

        PCollection<Order> orderFilterInvalidAmountAndPrice = orderRemoveInvalidOnlineOrders.apply(
            "Filter negative amount and price",
                Filter.by(new SerializableFunction<Order, Boolean>(){
                    @Override
                    public Boolean apply(Order input){
                        return  (input.getAmount() > 0 && input.getPrice() > 0);
                    }
                })
        );

        PCollection<Order> orderFilterInvalidPaymentMethod = orderFilterInvalidAmountAndPrice.apply(
            "Filter invalid payment method",
                Filter.by(new SerializableFunction<Order, Boolean>(){
                    @Override
                    public Boolean apply(Order input){
                        if(input.getBilling_info().getPayment_method().equals("CASH") && input.getBilling_info().getCard_number() != null) {
                            return false;
                        }
                        return true;
                    }
                })
        );
        
        PCollection<KV<Long, Order>> orderPrep = orderFilterInvalidPaymentMethod.apply(
                "Order join preparation", ParDo.of(new orderJoinTransformer())
        );
        PCollection<KV<Long, User>> userPrep = user.apply(
                "User join preparation", ParDo.of(new userJoinTransformer())
        );

        final TupleTag<Order> orderTag = new TupleTag<>();
        final TupleTag<User> userTag = new TupleTag<>();

        PCollection<KV<Long, CoGbkResult>> result =
                KeyedPCollectionTuple.of(orderTag, orderPrep).and(userTag, userPrep).apply("Join orders and users",CoGroupByKey.create());


        PCollection<KV<String, Long>> res = result.apply("Calculate country sum",ParDo.of(new DoFn<KV<Long, CoGbkResult>, KV<String, Long>>() {
            @ProcessElement
            public void processElement(ProcessContext c){
                KV<Long, CoGbkResult> e = c.element();
                CoGbkResult result = e.getValue();

                Iterable<Order> allOrders = result.getAll(orderTag);
                User user = result.getOnly(userTag);

                Long _sum = 0l;
                for(Order o: allOrders){
                    _sum += o.getAmount() * o.getPrice();
                }
                KV<String, Long> countrySum = KV.of(user.getCode(), _sum);

                c.output(countrySum);
            }
        }));

        PCollection<KV<String, Long>> countrySum = res.apply("",Combine.<String, Long, Long>perKey(
            Sum.ofLongs()
        ));

        PCollection<String> toPrint = countrySum.apply(ParDo.of(new DoFn<KV<String, Long>, String>(){
            @DoFn.ProcessElement
            public void processElement(@Element KV<String, Long> element, OutputReceiver<String> out) {
                String ouputStr = element.getKey().toString() + " " + element.getValue().toString();
                out.output(ouputStr);
            }
        }));


        toPrint.apply("Writing to Cloud Storage",TextIO.write().to(outMetricResultCountrySum));

        PCollection<KV<String,Long>> onlineVsBAM =  orderFilterInvalidPaymentMethod.apply(ParDo.of(new DoFn<Order, KV<String,Long>>(){
            @DoFn.ProcessElement
            public void processElement(@Element Order element, OutputReceiver<KV<String, Long>> out) {
                KV<String,Long> onlineVsBAM = KV.of(element.getOrder_type(), element.getAmount()*element.getPrice());
                out.output(onlineVsBAM);
            }
        }));

        PCollection<KV<String, Long>> onlineVsBamCombine = onlineVsBAM.apply(Combine.<String, Long, Long>perKey(
                Sum.ofLongs()
        ));

        PCollection<String> toPrint1 = onlineVsBamCombine.apply(ParDo.of(new DoFn<KV<String, Long>, String>(){
            @DoFn.ProcessElement
            public void processElement(@Element KV<String, Long> element, OutputReceiver<String> out) {
                String ouputStr = element.getKey().toString() + " " + element.getValue().toString();
                out.output(ouputStr);
            }
        }));

        toPrint1.apply(TextIO.write().to(outMetricResultOnlineVSBAM));

        p.run().waitUntilFinish();
    }
}
