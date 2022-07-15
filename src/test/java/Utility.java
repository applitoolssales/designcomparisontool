import com.applitools.eyes.RectangleSize;

import java.util.Map;

public class Utility {
    Map<String, String> params;

    public Utility(Map<String, String> params){
        this.params = params;
    }

    Boolean isVisualGridRunner(){
        return getConfigBool("UseVisualGrid");
    }

    Boolean getConfigBool(String configName){
        return Boolean.parseBoolean(getParam(configName));
    }

    String getParam(String paramName) {
        return System.getProperty(paramName) == null ? params.get(paramName) : System.getProperty(paramName);
    }

    RectangleSize translateViewport(String viewportString){
        String[] vpSize = viewportString.split("x");
        return new RectangleSize(Integer.parseInt(vpSize[0]), Integer.parseInt(vpSize[1]));
    }

    Integer translateViewport(String viewportString, String dimension){
        String[] vpSize = viewportString.split("x");
        Integer size = 0;

        if(dimension.equals("width")){
            size = Integer.parseInt(vpSize[0]);
        }

        if(dimension.equals("height")){
            size = Integer.parseInt(vpSize[1]);
        }

        return size;
    }

    String[] deserializeParam(String param){
        return getParam(param).split(";");
    }

}
