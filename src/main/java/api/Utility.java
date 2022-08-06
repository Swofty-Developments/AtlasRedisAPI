package api;

public class Utility {

      protected static void runAsync(Runnable runnable) {
            new Thread(runnable).start();
      }

}
