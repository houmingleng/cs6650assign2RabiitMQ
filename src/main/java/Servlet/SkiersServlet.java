package Servlet;

import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import models.ChannelPool;
import models.LiftRide;

//@WebServlet(name = "SkiersServlet", value = "/SkiersServlet")
public class SkiersServlet extends HttpServlet {
    private Gson gson = new Gson();
    private Message outputMsg = new Message("hello");
    private String skierID;
    private String resortID;
    private String dayID;
    private String seasonID;
    private String liftID;
    private  String time;

    private ChannelPool channelPool;

    private final static String QUEUE_NAME = "rpc_queue";

    public static class Message{
        String message;
        public Message(String msg) {
            message = msg;
        }
    }
    private enum HttpRequestStatus{
        GET_NO_PARAM,
        GET_SKIERS_WITH_RESORT_SEASON_DAY_ID,
        POST_SKIERS_WITH_RESORT_SEASON_DAY_ID,
        GET_VERTICAL_WITH_ID,
        POST_SEASONS_WITH_RESORT,
        NOT_VALID
    }
    @Override
    public void init() throws ServletException {
        try {
            System.out.println("begin");
            super.init();
            channelPool = new ChannelPool();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
    private boolean sendMessageToQue(String message) {
        try {
            Channel channel = channelPool.getChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME,
                    null,message.getBytes(StandardCharsets.UTF_8));
            channelPool.add(channel);
            return true;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void withNoParams(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        try {
            PrintWriter out = response.getWriter();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing params");
            return;
        }
       //System.out.println(urlPath);
        if (!urlValid(urlPath)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            out.write(gson.toJson(new Message(urlPath)));
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("works!");
        }
    }
    private boolean urlValid(String urlPath) {
        if(urlPath == null || urlPath.isEmpty()) return false;
        return true;
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        //System.out.println(urlPath);

        HttpRequestStatus curStatus = checkStatus(urlPath, req.getMethod());
        //System.out.println(curStatus.name());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        if(!curStatus.equals(HttpRequestStatus.NOT_VALID)) {
            res.setStatus(HttpServletResponse.SC_OK);
            if(curStatus.equals(HttpRequestStatus.GET_NO_PARAM)) withNoParams(res);
            else{
                String line;
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = req.getReader();
                try{
                    while((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);
                liftRide.setDayID(dayID);
                liftRide.setSkierID(skierID);
                liftRide.setSeasonID(seasonID);
                liftRide.setResortID(resortID);
                //System.out.println(liftRide);
                String message = gson.toJson(liftRide);
                if(sendMessageToQue(message)) {
                    res.getWriter().write(gson.toJson(new Message("active")));
                } else {
                    res.getWriter().write("not success");
                }
                res.getWriter().flush();
            }
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(outputMsg));
            out.flush();
        }
    }

    private HttpRequestStatus checkStatus(String urlPath, String type) {
        if(urlPath == null || urlPath.isEmpty()) return HttpRequestStatus.GET_NO_PARAM;
        String resortID = "";
        String seasons = "";
        String dayID = "";
        String skierID = "";
        String[] urlParts = urlPath.split("/");

        if(urlParts.length == 9) {
           // System.out.println(urlParts[3] +" "+ urlParts[5] +" "+ urlParts[1]);
            if(!urlParts[3].equals("seasons") || !urlParts[5].equals("days") || !urlParts[1].equals("skiers")) {
                return HttpRequestStatus.NOT_VALID;
            }
            resortID = urlParts[2];
            seasons = urlParts[4];
            dayID = urlParts[6];
            skierID = urlParts[8];
            //System.out.println(seasons +" "+ dayID +" "+ resortID+" "+skierID);
            if(!isValid(resortID) || isValid(dayID)
                    || !isValid(skierID)) {

                return HttpRequestStatus.NOT_VALID;
            }
            this.resortID = resortID;
            this.seasonID = seasons;
            this.dayID = dayID;
            this.skierID = skierID;
            if(type.equals("GET"))
                return HttpRequestStatus.GET_SKIERS_WITH_RESORT_SEASON_DAY_ID;
            else return HttpRequestStatus.POST_SKIERS_WITH_RESORT_SEASON_DAY_ID;

        } else if(urlParts.length == 3) {
            if(!urlParts[2].equals("vertical")) {
                return HttpRequestStatus.NOT_VALID;
            }
            resortID = urlParts[1];
            if(!isValid(resortID)) {
                return HttpRequestStatus.NOT_VALID;
            }
            return HttpRequestStatus.GET_VERTICAL_WITH_ID;
        } else {
            return HttpRequestStatus.NOT_VALID;
        }
    }

    private boolean isValid(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            int digits = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
