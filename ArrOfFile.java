/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arhivator;

import java.io.File;

/**
 *
 * @author roman_davydov
 */
class ArrOfFile {
    
      static int i = 1;
    int num;
    String path;
    File f;
    long d;

    
    public ArrOfFile(String path) {
        this.path = path;
        this.f = new File(path);
        this.d = this.f.lastModified();
        this.num = i++;
    }

    public String toString() {

        return "num=" + num + "; d= " + d + "; String=" + path + "; f=" + f.toString();
    }
    
}
