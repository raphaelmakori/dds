package com.dds.client;

import com.dds.shared.AdminReport;
import com.dds.shared.Branch;
import com.dds.shared.CustomerOrder;
import com.dds.shared.DrinkBrand;
import com.dds.shared.OrderItem;
import com.dds.shared.Request;
import com.dds.shared.RequestType;
import com.dds.shared.Response;

import java.util.Arrays;

public class DemoScenario {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5050;

        try (ServerConnection connection = new ServerConnection(host, port)) {
            place(connection, new CustomerOrder("Alice", Branch.NAKURU, Arrays.asList(
                    new OrderItem(DrinkBrand.COKE, 5),
                    new OrderItem(DrinkBrand.WATER, 2)
            )));
            place(connection, new CustomerOrder("Brian", Branch.MOMBASA, Arrays.asList(
                    new OrderItem(DrinkBrand.JUICE, 4),
                    new OrderItem(DrinkBrand.FANTA, 3)
            )));
            place(connection, new CustomerOrder("Carol", Branch.KISUMU, Arrays.asList(
                    new OrderItem(DrinkBrand.SPRITE, 31)
            )));
            place(connection, new CustomerOrder("Diana", Branch.NAIROBI, Arrays.asList(
                    new OrderItem(DrinkBrand.COKE, 6)
            )));

            Response reportResponse = connection.send(new Request(RequestType.GET_REPORT, null));
            if (reportResponse.isSuccess()) {
                ReportPrinter.print((AdminReport) reportResponse.getPayload());
            }
        } catch (Exception exception) {
            System.out.println("Demo scenario failed: " + exception.getMessage());
        }
    }

    private static void place(ServerConnection connection, CustomerOrder order) throws Exception {
        Response response = connection.send(new Request(RequestType.PLACE_ORDER, order));
        System.out.println(response.getMessage());
    }
}
