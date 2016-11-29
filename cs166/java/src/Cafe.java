/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;



/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   //login info for later use
   private static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe (String dbname, String dbport) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://127.0.0.1:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Cafe

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 2) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Cafe.class.getName () +
            " <dbname> <port>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         esql = new Cafe (dbname, dbport);

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              String user_type = find_type(esql);
	      switch (user_type){
		case "Customer": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Order History");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: UpdateOrder(esql); break;
                       case 5: ViewOrderHistory(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Employee": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: UpdateUserInfo(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
		case "Manager ": 
		  while(usermenu) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Browse Menu by ItemName");
                    System.out.println("2. Browse Menu by Type");
                    System.out.println("3. Add Order");
                    System.out.println("4. Update Order");
                    System.out.println("5. View Current Orders");
                    System.out.println("6. View Order Status");
                    System.out.println("7. Update User Info");
                    System.out.println("8. Update Menu");
                    System.out.println(".........................");
                    System.out.println("9. Log out");
                      switch (readChoice()){
                       case 1: BrowseMenuName(esql); break;
                       case 2: BrowseMenuType(esql); break;
                       case 3: AddOrder(esql); break;
                       case 4: EmployeeUpdateOrder(esql); break;
                       case 5: ViewCurrentOrder(esql); break;
                       case 6: ViewOrderStatus(esql); break;
                       case 7: ManagerUpdateUserInfo(esql); break;
                       case 8: UpdateMenu(esql); break;
                       case 9: usermenu = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		      }//end switch
		  } break;
	      }//end switch
            }//end if
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface                         \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();
         
	 String type="Customer";
	 String favItems="";

	 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end
   
   public static boolean userExists(Cafe esql, String login){
      try{
         String query = String.format("SELECT * FROM Users WHERE login = '%s'", login);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return true;
         return false;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return false;
      }
   }//end

   public static String find_type(Cafe esql){
      // Your code goes here.
      // ...
      try{
         String query = String.format("SELECT type FROM Users WHERE login = '%s'", esql.authorisedUser);
         List<List<String>> tuples = esql.executeQueryAndReturnResult(query);
         // ...
         return tuples.get(0).get(0);
      }
      catch(Exception e) {
         System.err.println (e.getMessage ());
         return null;
      }
   }

   public static void BrowseMenuName(Cafe esql){
      // Your code goes here.
      // ...
      try{
         String query = String.format("SELECT * FROM Menu ORDER BY itemName" );
         esql.executeQueryAndPrintResult(query);
      }
      // ...
      catch(Exception e) {
         System.err.println (e.getMessage ());
      }
   }//end

   public static void BrowseMenuType(Cafe esql){
      // Your code goes here.
      // ...
      try {
         String query = String.format("SELECT * FROM Menu ORDER BY type" );
         esql.executeQueryAndPrintResult(query);
      }
      // ...
      catch(Exception e) {
         System.err.println (e.getMessage ());
      }
   }//end
   
   public static String itemExists(Cafe esql){
      try{
         System.out.print("\tEnter item name: ");
         String name = in.readLine();

         String query = String.format("SELECT * FROM Menu WHERE itemName = '%s'", name);
         int numOfItem = esql.executeQuery(query);
	 if (numOfItem > 0)
		return name;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end
   
   public static String itemInOrder(Cafe esql, orderid){
      try{
         System.out.print("\tEnter item name: ");
         String name = in.readLine();

         String query = String.format("SELECT * FROM ItemStatus WHERE itemName = '%s' AND orderid = '%s", name, orderid);
         int numOfItem = esql.executeQuery(query);
	 if (numOfItem > 0)
		return name;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end
   
   public static Integer AddOrder(Cafe esql){
      // Your code goes here.
      // ...
      try {
         List<String> orderItems = new ArrayList<String>();
         List<String> comments = new ArrayList<String>();
         while(true){
            String item = itemExists(esql);
            orderItems.add(item);
            System.out.print("Any additional comments? [y/n] ");
            String temp = in.readLine();
            if(temp == "y" || temp == "Y") {
               System.out.println("Enter comments: ");
               comments.add(in.readLine());
               
            }
            else {
               comments.add("");
            }
            
            System.out.print("\tAdd another item? [y/n] ");
            String response = in.readLine();
            if(response != "y" && response != "Y")
               break;
         }
         
         double total = 0;
         //get the total
         for(int i = 0; i < orderItems.size(); ++i){
            String query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", orderItems.get(i));
            List<List<String>> tempTup = esql.executeQueryAndReturnResult(query);
            total += Double.parseDouble(tempTup.get(0).get(0));
         }
         
         System.out.print("\tWill you be paying your total of " + total + " at this time? [y/n]");
         String response = in.readLine();
         
         boolean paid = true;
         if(response != "y" && response != "Y") {
            paid = false;
         }
         
         Timestamp placedAt = new Timestamp(System.currentTimeMillis());
         
         String query = String.format("INSERT INTO Orders (orderid, login, paid, timeStampRecieved, total) VALUES (DEFAULT,'%s','%s','%s','%s')", esql.authorisedUser, paid, placedAt, total);
         
         esql.executeUpdate(query);
         System.out.print ("Order successfully added! OrderID: ");
         //get order id 
         String query2 = String.format("SELECT MAX(orderid) FROM Orders");
         List<List<String>> tempTup = esql.executeQueryAndReturnResult(query2);
         String orderid = tempTup.get(0).get(0);
         System.out.println(orderid);
         //fill itemStatus with each item in the order
         String status = "incomplete";
         for(int i = 0; i < orderItems.size(); ++i){
            String query3 = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ('%s','%s','%s','%s','%s')", orderid, orderItems.get(i), placedAt, status, comments.get(i));
            esql.executeUpdate(query3); 
         }
         
         return Integer.parseInt(orderid);
      }
      
      catch(Exception e) {
         System.err.println (e.getMessage ());
         return null;
      }
      // ...
   }//end 

   public static void UpdateOrder(Cafe esql){
      // Your code goes here.
      // ...
         System.out.println ("Enter the orderid of the order you wish to update: ");
         String inp = in.readLine();

         String query = String.format("SELECT * FROM Orders WHERE orderid = '%s'", inp);
         int numOfTuples = esql.executeQuery(query);
         if (numOfTuples < 1) {
            System.out.println ("Error, invalid order id");
         }
         else {
            boolean updatingOrder = true;
            while(updatingOrder)
            System.out.println("UPDATING ORDER # " + inp);
            System.out.println("---------");
            System.out.println("1. Delete order");
            System.out.println("2. Pay order");
            System.out.println("3. View Items");
            System.out.println("4. Add Items");
            System.out.println("5. Remove Items");
            System.out.println("9. Cancel");
            switch (readChoice()){
               case 1: //delete order
                  String query = String.format("DELETE FROM Orders WHERE orderid = '%s'", inp);
                  esql.executeUpdate(query);
                  break;
               case 2: //pay order
                  String query = String.format("UPDATE Orders SET paid = 'true' WHERE orderid = '%s'", inp);
                  esql.executeUpdate(query);
                  break;
               case 3: //view items;
                  String query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%s'", inp);
                  esql.executeQueryAndPrintResult(query);
                  break;
               case 4: // add items;
                  //check if the order has already been paid for
                  String query = String.format("SELECT paid FROM Orders WHERE orderid = '%s'", inp);
                  List<List<String>> paidTup = esql.executeQueryAndReturnResult(query);
                  String canAdd = (tempTup.get(0).get(0));
                  if(canAdd == "true") {
                     System.out.println("You cannot add more items after your order has been paid. Please place a new order.");
                     break;
                  }
                  try {
                     List<String> orderItems = new ArrayList<String>();
                     List<String> comments = new ArrayList<String>();
                     while(true){
                        String item = itemExists(esql);
                        orderItems.add(item);
                        System.out.print("Any additional comments? [y/n] ");
                        String temp = in.readLine();
                        if(temp == "y" || temp == "Y") {
                           System.out.println("Enter comments: ");
                           comments.add(in.readLine());
                           
                        }
                        else {
                           comments.add("");
                        }
                        
                        System.out.print("\tAdd another item? [y/n] ");
                        String response = in.readLine();
                        if(response != "y" && response != "Y")
                           break;
                     }
                     
                     double total = 0;
                     //get the total
                     for(int i = 0; i < orderItems.size(); ++i){
                        String query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", orderItems.get(i));
                        List<List<String>> paidTup = esql.executeQueryAndReturnResult(query);
                        total += Double.parseDouble(paidTup.get(0).get(0));
                     }
                     //update the order total
                     String queryTotal = String.format("UPDATE Orders SET total = total + '%s' WHERE orderid = '%s'", total, inp);
                     esql.executeUpdate(queryTotal);
                  
                     Timestamp placedAt = new Timestamp(System.currentTimeMillis());
                     //fill itemStatus with each item in the order
                     String status = "incomplete";
                     for(int i = 0; i < orderItems.size(); ++i){
                        String query3 = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ('%s','%s','%s','%s','%s')", inp, orderItems.get(i), placedAt, status, comments.get(i));
                        esql.executeUpdate(query3); 
                     }
                  }
                  catch(Exception e) {
                     System.err.println (e.getMessage ());
                     return null;
                  }
                        break;
               case 5: //remove items;
                  //check if the order has already been paid for
                  String query = String.format("SELECT paid FROM Orders WHERE orderid = '%s'", inp);
                  List<List<String>> paidTup = esql.executeQueryAndReturnResult(query);
                  String canAdd = (paidTup.get(0).get(0));
                  if(canAdd == "true") {
                     System.out.println("Sorry, but you cannot remove items after your order has been paid.");
                     break;
                  }
                  
                  try {
                     List<String> orderItems = new ArrayList<String>();
                     while(true){
                        String item = itemInOrder(esql, inp);
                        orderItems.add(item);
                        System.out.print("\tRemove another item? [y/n] ");
                        String response = in.readLine();
                        if(response != "y" && response != "Y")
                           break;
                     }
                     
                     double total = 0;
                     //get the total
                     for(int i = 0; i < orderItems.size(); ++i){
                        String query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", orderItems.get(i));
                        List<List<String>> tempTup = esql.executeQueryAndReturnResult(query);
                        total += Double.parseDouble(tempTup.get(0).get(0));
                     }
                     //update the order total
                     String queryTotal = String.format("UPDATE Orders SET total = total - '%s' WHERE orderid = '%s'", total, inp);
                     esql.executeUpdate(queryTotal);
                  
                     //remove from itemStatus the items to be remove
                     for(int i = 0; i < orderItems.size(); ++i){
                        String query3 = String.format("DELETE FROM ItemStatus WHERE orderid = '%s' AND itemName = '%s'", inp, orderItems.at(i));
                        esql.executeUpdate(query3); 
                     }
                  }
                  
                  catch(Exception e) {
                     System.err.println (e.getMessage ());
                     return null;
                  }
                        break;
               case 9: updatingOrder = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            
            }
         }
      // ...
   }//end

   public static void EmployeeUpdateOrder(Cafe esql){
      // Your code goes here.
      // ...
      System.out.println ("Enter the orderid of the order you wish to update: ");
      String inp = in.readLine();

      String query = String.format("SELECT * FROM Orders WHERE orderid = '%s'", inp);
      int numOfTuples = esql.executeQuery(query);
      if (numOfTuples < 1) {
         System.out.println ("Error, invalid order id");
      }
      
      else {
         boolean updatingOrder = true;
         while(updatingOrder){
         System.out.println("UPDATING ORDER # " + inp);
         System.out.println("---------");
         System.out.println("1. Change Order to Paid");
         System.out.println("2. Complete Item Status");
         System.out.println("3. View Items");
         System.out.println("4. Add Items");
         System.out.println("5. Remove Items");
         System.out.println("9. Cancel");
         switch (readChoice()){
            case 1: //update order to paid
               String query = String.format("UPDATE Orders SET paid = 'true' WHERE orderid = '%s'", inp);
               esql.executeUpdate(query);
               break;
            case 2:
               String query = String.format("UPDATE ItemStatus SET status = 'completed' WHERE orderid = '%s'", inp);
               esql.executeUpdate(query);
               break;
            case 4: // add items;
               //check if the order has already been paid for
               String query = String.format("SELECT paid FROM Orders WHERE orderid = '%s'", inp);
               List<List<String>> paidTup = esql.executeQueryAndReturnResult(query);
               String canAdd = (paidTup.get(0).get(0));
               if(canAdd == "true") {
                  System.out.println("You cannot add more items after your order has been paid. Please place a new order.");
                  break;
               }
               
               try {
                  List<String> orderItems = new ArrayList<String>();
                  List<String> comments = new ArrayList<String>();
                  while(true){
                     String item = itemExists(esql);
                     orderItems.add(item);
                     System.out.print("Any additional comments? [y/n] ");
                     String temp = in.readLine();
                     if(temp == "y" || temp == "Y") {
                        System.out.println("Enter comments: ");
                        comments.add(in.readLine());
                        
                     }
                     else {
                        comments.add("");
                     }
                     
                     System.out.print("\tAdd another item? [y/n] ");
                     String response = in.readLine();
                     if(response != "y" && response != "Y")
                        break;
                  }
                  
                  double total = 0;
                  //get the total
                  for(int i = 0; i < orderItems.size(); ++i){
                     String query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", orderItems.get(i));
                     List<List<String>> tempTup = esql.executeQueryAndReturnResult(query);
                     total += Double.parseDouble(tempTup.get(0).get(0));
                  }
                  //update the order total
                  String queryTotal = String.format("UPDATE Orders SET total = total + '%s' WHERE orderid = '%s'", total, inp);
                  esql.executeUpdate(queryTotal);
               
                  Timestamp placedAt = new Timestamp(System.currentTimeMillis());
                  //fill itemStatus with each item in the order
                  String status = "incomplete";
                  for(int i = 0; i < orderItems.size(); ++i){
                     String query3 = String.format("INSERT INTO ItemStatus (orderid, itemName, lastUpdated, status, comments) VALUES ('%s','%s','%s','%s','%s')", inp, orderItems.get(i), placedAt, status, comments.get(i));
                     esql.executeUpdate(query3); 
                  }
               }
               
               catch(Exception e) {
                  System.err.println (e.getMessage ());
                  return null;
               }
               
                     break;
            case 5: //remove items;
               //check if the order has already been paid for
               String query = String.format("SELECT paid FROM Orders WHERE orderid = '%s'", inp);
               List<List<String>> paidTup = esql.executeQueryAndReturnResult(query);
               String canAdd = (paidTup.get(0).get(0));
               if(canAdd == "true") {
                  System.out.println("Sorry, but you cannot remove items after your order has been paid.");
                  break;
               }
               
               try {
                  List<String> orderItems = new ArrayList<String>();
                  while(true){
                     String item = itemInOrder(esql, inp);
                     orderItems.add(item);
                     System.out.print("\tRemove another item? [y/n] ");
                     String response = in.readLine();
                     if(response != "y" && response != "Y")
                        break;
                  }
                  
                  double total = 0;
                  //get the total
                  for(int i = 0; i < orderItems.size(); ++i){
                     String query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", orderItems.get(i));
                     List<List<String>> tempTup = esql.executeQueryAndReturnResult(query);
                     total += Double.parseDouble(tempTup.get(0).get(0));
                  }
                  //update the order total
                  String queryTotal = String.format("UPDATE Orders SET total = total - '%s' WHERE orderid = '%s'", total, inp);
                  esql.executeUpdate(queryTotal);
               
                  //fill itemStatus with each item in the order
                  for(int i = 0; i < orderItems.size(); ++i){
                     String query3 = String.format("DELETE FROM ItemStatus WHERE orderid = '%s' AND itemName = '%s'", inp, orderItems.at(i));
                     esql.executeUpdate(query3); 
                  }
               }
               
               catch(Exception e) {
                  System.err.println (e.getMessage ());
                  return null;
               }
                     break;
            case 9: updatingOrder = false; break;
            default : System.out.println("Unrecognized choice!"); break;
         
         }
      }
      // ...
   }//end

   public static void ViewOrderHistory(Cafe esql){
      // Your code goes here.
      // ...
      String query = String.format("SELECT TOP(5) * FROM Orders WHERE login = '%s' ORDER BY orderid", esql.authorisedUser);
      esql.executeQueryAndPrintResult(query);
      // ...
   }//end

   public static void UpdateUserInfo(Cafe esql){
      // Your code goes here.
      // ...
      boolean updatingInfo = true;
      while(updatingInfo) {
                    System.out.println("MAIN MENU");
                    System.out.println("---------");
                    System.out.println("1. Change Password");
                    System.out.println("2. Edit Favorite Items");
                    System.out.println("3. Change Phone Number");
                    System.out.println(".........................");
                    System.out.println("9. Cancel");
                      switch (readChoice()){
                       case 1: 
                           System.out.print("Enter new password: ");
                           String newpass = in.readLine();
                           String query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", newpass, esql.authorisedUser);
                           esql.executeUpdate(query);
                           break;
                       case 2: 
                           System.out.println("Enter new favorite items");
                           String newItems = in.readLine();
                           String query = String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'", newItems, esql.authorisedUser);
                           esql.executeUpdate(query);
                           break;
                       case 3: 
                           System.out.print("Enter new phone number: ");
                           String newnum = in.readLine();
                           String query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", newnum, esql.authorisedUser);
                           esql.executeUpdate(query);
                           break;
                       case 9: updatingInfo = false; break;
                       default : System.out.println("Unrecognized choice!"); break;
		             }//end switch
      }
      // ...
   }//end

   public static void ManagerUpdateUserInfo(Cafe esql){
      // Your code goes here.
      // ...
      boolean updatingInfo = true;
      System.out.print("Enter login of User to Update: ");
      String userlogin = in.readLine();
      
      if(userExists(esql, userlogin) == false) {
         System.out.print("Incorrect user login!");
         break;
      }
      
      while(updatingInfo) {
        System.out.println("Updating Account");
        System.out.println("---------");
        System.out.println("1. Change Password");
        System.out.println("2. Edit Favorite Items");
        System.out.println("3. Change Phone Number");
        System.out.println("4. Change Type of User");
        System.out.println(".........................");
        System.out.println("9. Cancel");
          switch (readChoice()){
           case 1: 
               System.out.print("Enter new password: ");
               String newpass = in.readLine();
               String query = String.format("UPDATE Users SET password = '%s' WHERE login = '%s'", newpass, userlogin);
               esql.executeUpdate(query);
               break;
           case 2: 
               System.out.println("Enter new favorite items");
               String newItems = in.readLine();
               String query = String.format("UPDATE Users SET favItems = '%s' WHERE login = '%s'", newItems, userlogin);
               esql.executeUpdate(query);
               break;
           case 3: 
               System.out.print("Enter new phone number: ");
               String newnum = in.readLine();
               String query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", newnum, userlogin);
               esql.executeUpdate(query);
               break;
           case 4:
               System.out.print("Enter new Type for the User: ");
               String newtype = in.readLine();
               String query = String.format("UPDATE Users SET type = '%s' WHERE login = '%s'", newtype, userlogin);
               esql.executeUpdate(query);
               break;
           case 9: updatingInfo = false; break;
           default : System.out.println("Unrecognized choice!"); break;
       }//end switch
      }
      // ...
   }//end

   public static void UpdateMenu(Cafe esql){
      // Your code goes here.
      boolean updatingMenu = true;
      while(updatingMenu) {
         System.out.println("Updating Menu");
         System.out.println("---------");
         System.out.println("1. Add Item");
         System.out.println("2. Delete Item");
         System.out.println("3. Update Item");
         System.out.println(".........................");
         System.out.println("9. Cancel");
         switch (readChoice()){
            case 1: // add item
               String itemName = itemExists(esql);
               System.out.println("Enter Item Type: ");
               String type = in.readLine();
               System.out.println("Enter Item Description: ");
               String description = in.readLine();
               System.out.println("Enter Item imageURL: ");
               String imageURL = in.readLine();
               System.out.println("Enter Item Price: ");
               String price = in.readLine();
               String query = String.format("INSERT INTO Menu (itemName, type, price, description, imageURL) VALUES ('%s','%s','%s','%s','%s')", itemName, type, price, description, imageURL;
               esql.executeUpdate(query); 
               break;
            case 2: //delete item
               String name = itemExists(esql);
               String query = String.format("DELETE FROM Menu WHERE itemName = '%s'", name);
               esql.executeUpdate(query);
               break;
            case 3: //update item
               String itemName = itemExists(esql);
               System.out.println("Updating Item");
               System.out.println("---------");
               System.out.println("1. Update Name");
               System.out.println("2. Update Type");
               System.out.println("3. Update Price");
               System.out.println("4. Update Description");
               System.out.println("5. Update ImageURL");
               System.out.println(".........................");
               System.out.println("9. Cancel");
               
               switch(readChoice()) {
                  case 1: 
                     String newName = itemExists(esql);
                     String query = String.format("UPDATE Menu SET itemName = '%s' WHERE itemName = '%s'", newName, itemName);
                     esql.executeUpdate(query);
                     break;
                  case 2: 
                     System.out.println("Enter New Type");
                     String newType = in.readLine();
                     String query = String.format("UPDATE Menu SET type = '%s' WHERE itemName = '%s'", newType, itemName);
                     esql.executeUpdate(query);
                     break;
                  case 3: 
                     System.out.print("Enter New Price: ");
                     String newPrice = in.readLine();
                     String query = String.format("UPDATE Menu SET price = '%s' WHERE itemName = '%s'", newPrice, itemName);
                     esql.executeUpdate(query);
                     break;
                  case 4:
                     System.out.print("Enter New Description: ");
                     String newDesc = in.readLine();
                     String query = String.format("UPDATE Menu SET description = '%s' WHERE itemName = '%s'", newDesc, itemName);
                     esql.executeUpdate(query);
                     break;
                  case 5: 
                     System.out.print("Enter New ImageURL: ");
                     String newURL = in.readLine();
                     String query = String.format("UPDATE Menu SET imageURL = '%s' WHERE itemName = '%s'", newURL, itemName);
                     esql.executeUpdate(query);
                  default: 
                     System.out.println("Unrecognized choice!"); 
                     break;
                  case 9: break;
               }
               
               break; // case 3 break 
               
            case 9: 
               updatingMenu = false; 
               break;
               
            default: 
               System.out.println("Unrecognized choice!"); 
               break;
         }
      // ...
   }//end

   public static void ViewOrderStatus(Cafe esql){
      // Your code goes here.
      // ...
      String query = String.format("SELECT paid FROM Orders WHERE orderid = '%s'", inp);
      System.out.print("Order number " + inp + " paid? ")
      esql.executeQueryAndPrintResult(query);
      String query2 = String.format("SELECT * FROM ItemStatus WHERE orderid = '%s'", inp);
      esql.executeQueryAndPrintResult(query2);
      // ...
   }//end

   public static void ViewCurrentOrder(Cafe esql){
      // Your code goes here.
      // ...
      Timestamp now = new Timestamp(System.currentTimeMillis());
      orderStatus = "false"
      String query = String.format("SELECT * FROM Orders WHERE timeStampRecieved > now - 8.64*10^7 AND paid = '%s' ORDER BY orderid", orderStatus);
      esql.executeQueryAndPrintResult(query);
      // ...
   }//end
   
}//end Cafe
