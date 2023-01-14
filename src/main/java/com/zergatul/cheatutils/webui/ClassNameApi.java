package com.zergatul.cheatutils.webui;

import net.minecraft.client.MinecraftClient;
import org.apache.http.HttpException;

public class ClassNameApi extends ApiBase {

    @Override
    public String getRoute() {
        return "class-name";
    }

    @Override
    public String get(String className) throws HttpException {
        try {
            Class.forName(className, false, MinecraftClient.class.getClassLoader());
        }
        catch (ClassNotFoundException e) {
            throw new NotFoundHttpException("Class not found");
        }

        return "{ \"ok\": true }";
    }
}