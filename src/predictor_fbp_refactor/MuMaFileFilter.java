package predictor_fbp_refactor;


import java.io.File;
import javax.swing.filechooser.FileFilter;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author valer
 */
public class MuMaFileFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
      if (file.isDirectory()) return true;
        String fname = file.getName().toLowerCase();
        return fname.endsWith("txt") || fname.endsWith("muma");
    }

    @Override
    public String getDescription() {
      return "A file compatible with MuMaPredictor";
    }
  }
