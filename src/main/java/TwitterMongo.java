
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.bson.Document;
import static java.util.Arrays.asList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.UUID;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author Kevin
 */
public class TwitterMongo {
    private MongoClient mongoclient;
    private MongoDatabase db;
    private String loginusername = "";
    public TwitterMongo(){
        mongoclient = new MongoClient("167.205.35.19",27017);
        db = mongoclient.getDatabase("13512096");
    }
    public TwitterMongo(String hostip,int port,String database){
        mongoclient = new MongoClient(hostip,port);
        db = mongoclient.getDatabase(database);
    }
    public void insertnewuser(String username,String password){
        db.getCollection("users").insertOne(
                new Document("username",username)
                .append("password",password)
        );
    }
    
    public void insertnewfriend(String username,String friendname,Date timestamp){
        db.getCollection("friends").insertOne(
                new Document("username",username)
                .append("friend",friendname)
                .append("timestamp",timestamp)
        );
    }
    
    public void insertnewfollower(String username,String followername,Date timestamp){
        db.getCollection("followers").insertOne(
                new Document("username",username)
                .append("follower",followername)
                .append("timestamp",timestamp)
        );
    }
    
    public void insertnewtweet(UUID tweet_id,String username,String tweet_body){
        db.getCollection("tweets").insertOne(
                new Document("tweet_id",tweet_id)
                .append("username",username)
                .append("body",tweet_body)
        );
    }
    
    public void insertnewuserline(String username,Date timeuuid,UUID tweet_id){
        db.getCollection("userline").insertOne(
                new Document("username",username)
                .append("time",timeuuid)
                .append("tweet_id",tweet_id)
        );
    }
    
    public void insertnewtimeline(String username,Date timeuuid,UUID tweet_id){
        db.getCollection("timeline").insertOne(
                new Document("username",username)
                .append("time",timeuuid)
                .append("tweet_id",tweet_id)
        );
    }
    
    public void Login(String username,String password){
        if(loginusername.equals("")){
            FindIterable<Document> iterable = null;
            iterable = db.getCollection("users").find(and(eq("username",username),(eq("password",password))));
            if(iterable.first() != null){
                System.out.println("Login as "+username);
            }
            else{
                System.out.println("Login failed!");
            }
            loginusername = username;
        }else{
            System.out.println("You are already login as : "+loginusername);
        }
    }
    
    public void register(String username,String password){
        FindIterable<Document> iterable = null;
        iterable = db.getCollection("users").find(eq("username",username));
        if(iterable.first() != null){
            System.out.println("User "+username+" already registered!");
        }
        else{
             insertnewuser(username, password);
             System.out.println("Registered "+username);
        }
        
    }
    
    public void follow(String followertoname){
        if(!loginusername.equals("")){
            FindIterable<Document> iterable = null;
            iterable = db.getCollection("users").find(eq("username",followertoname));
            if(iterable.first() != null){
                insertnewfollower(followertoname,loginusername,new Date());
                insertnewfriend(loginusername, followertoname,new Date());
                System.err.println(followertoname +" get follower "+loginusername);
                System.out.println(loginusername + " added "+followertoname+" as friend");
            }
            else{
                 System.out.println("User : "+followertoname+" doesn't exist!");
            }
            
            
        }else{
            System.out.println("You must login first!");
        }
    }
    
    public void tweetatweet(String tweet_body){
        if(!loginusername.equals("")){
            final UUID tweet_id = UUID.randomUUID();
            insertnewtweet(tweet_id, loginusername, tweet_body);
            insertnewuserline(loginusername,new Date(),tweet_id);
            insertnewtimeline(loginusername,new Date(), tweet_id);
            FindIterable<Document> iterable = null;
            iterable = db.getCollection("followers").find(eq("username",loginusername));
            iterable.forEach(new Block<Document>(){

                @Override
                public void apply(Document document) {
                    insertnewtimeline(document.get("follower").toString(),new Date(), tweet_id);
                }
            });
            System.out.println(loginusername +" tweeted : "+tweet_body);
        }else{
            System.out.println("You must login first!");
        }
    }
    
    public void showTweet(String username){
        FindIterable<Document> iterable = null;
        iterable = db.getCollection("userline").find(eq("username",username));
        iterable.forEach(new Block<Document>(){

            @Override
            public void apply(Document document) {
                UUID tweet_id = (UUID) document.get("tweet_id");
                final Date tweetdate = (Date) document.get("time");
                FindIterable<Document> iterable = null;
                iterable = db.getCollection("tweets").find(eq("tweet_id",tweet_id));
                iterable.forEach(new Block<Document>(){

                    @Override
                    public void apply(Document t) {
                        System.out.println(tweetdate + " [" + t.get("username").toString()+"] "+t.get("body").toString());
                    }
                    
                });;
            }
        });
    }
    
    public void showTimeline(){
        FindIterable<Document> iterable = null;
        iterable = db.getCollection("timeline").find(eq("username",loginusername));
        iterable.forEach(new Block<Document>(){
            @Override
            public void apply(Document document) {
                UUID tweet_id = (UUID) document.get("tweet_id");
                final Date tweetdate = (Date) document.get("time");
                FindIterable<Document> iterable = null;
                iterable = db.getCollection("tweets").find(eq("tweet_id",tweet_id));
                iterable.forEach(new Block<Document>(){

                    @Override
                    public void apply(Document t) {
                        System.out.println(tweetdate + " [" + t.get("username").toString()+"] "+t.get("body").toString());
                    }
                    
                });
            }
        });
    }
    
    public void logOut(){
        if(loginusername.equals("")){
            loginusername = "";
            System.out.println("Logout successed!");
        }else{
            System.out.println("Please login first!");
        }
    }
    
    public void setLoginUsername(String username){
        loginusername = username;
    }
    
    public String getLoginUsername(){
        return loginusername;
    }
    
    public static void main(String[] args) throws ParseException, Exception{
        TwitterMongo twittermongo = new TwitterMongo();
        CommandParser cmd = new CommandParser();
        Scanner sc = new Scanner(System.in);
        System.out.println("Please Log In !");
        String userinput = sc.nextLine();
        while(!userinput.equalsIgnoreCase("/exit")){
            List<String> commandnArgs = cmd.getCommand(userinput);
            if(!commandnArgs.isEmpty()){
                switch(commandnArgs.get(0)){
                    case "/register" : 
                        if(commandnArgs.get(1).contains(" ")){
                            String[] argument = commandnArgs.get(1).split(" ",2);
                            twittermongo.register(argument[0],argument[1]);
                        }
                        break;
                    case "/login" :
                        if(commandnArgs.get(1).contains(" ")){
                            String[] argument = commandnArgs.get(1).split(" ",2);
                            twittermongo.Login(argument[0],argument[1]);
                        }
                        break;
                    case "/tweet" :
                        twittermongo.tweetatweet(commandnArgs.get(1).toString());
                        break;
                    case "/follow" :
                        twittermongo.follow(commandnArgs.get(1).toString());
                        break;
                    case "/logout" :
                        twittermongo.logOut();
                        break;
                    case "/showtimeline" :
                        twittermongo.showTimeline();
                        break;
                    case "/showtweet" :
                        twittermongo.showTweet(commandnArgs.get(1).toString());
                        break;
                    case "/clear" :
                        twittermongo.clearAllTable();
                        break;
                    default : 
                        System.out.println("Command not recognized!");
                        break;
                }
            }else{
                System.out.println("Command not recognized!");
            }
            userinput = sc.nextLine();
        }
        
        
    }
    public void clearAllTable(){
        db.getCollection("users").drop();
        db.getCollection("friends").drop();
        db.getCollection("followers").drop();
        db.getCollection("tweets").drop();
        db.getCollection("userline").drop();
        db.getCollection("timeline").drop();
        System.out.println("All data in all table cleared!");
    }
}
