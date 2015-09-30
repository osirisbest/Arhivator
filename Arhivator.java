/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arhivator;

import java.io.IOException;

import javax.swing.SwingUtilities;

/**
 *
 * @author roman_davydov
 */
public class Arhivator
{

	volatile static Form form;

	public static void main(String[] args) throws IOException, InterruptedException {
		// /*
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				form = new Form();
			}
		});
	}

}
