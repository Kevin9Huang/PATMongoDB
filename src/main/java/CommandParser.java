
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kevin
 */
public class CommandParser {
    private static String[] commands = {"/clear","/register","/login","/logout","/tweet","/follow","/showtimeline","/showtweet","/exit"};
    public List<String> getCommand(String input){
        List<String> args = new ArrayList<String>();
        for(int i = 0;i< commands.length;i++){
            if(input.toLowerCase().contains(commands[i])){
                args.add(commands[i]);
                if(input.contains(" ")){
                    args.add(input.substring(commands[i].length()+1));
                }
                break;
            }
        }
        return args;
    }
}
