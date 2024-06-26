/**********************************************************************
* Copyright (c) by Alexander Dippe
* This program is free software: you can redistribute it and/or modify it under the terms of the 
* GNU General Public License as published by the Free Software Foundation, either version 3 of the 
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
* even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
* the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with this program.  If not, 
* see <http://www.gnu.org/licenses/>.
* 
* https://openjverein.github.io
**********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * E-Mail senden Formular anhand des zugewiesenen Spenders
 */
public class SpendenbescheinigungEmailAction implements Action
{

  /**
   * Versenden einer E-Mail anhand der Spendenbescheinigung die in der View
   * markiert ist.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Spendenbescheinigung spb = null;
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null)
    {
      throw new ApplicationException("Keine Spendenbescheinigung ausgewählt.");
    }
    else if (context instanceof Spendenbescheinigung)
    {
      spb = (Spendenbescheinigung) context;
    }
    else
    {
      return;
    }
    try
    {
      Mitglied member = spb.getMitglied();
      if (member == null || member.getEmail() == null
          || member.getEmail().length() == 0)
      {
        String fehler = "Kein Mitglied zugewiesen";
        GUI.getStatusBar().setErrorText(fehler);
        Logger.error(fehler);
      }
      if (member.getEmail() == null || member.getEmail().length() == 0)
      {
        String fehler = "Mitglied hat keine E-Mail Adresse";
        GUI.getStatusBar().setErrorText(fehler);
        Logger.error(fehler);
      }
      MitgliedMailSendenAction mailSendenAction = new MitgliedMailSendenAction();
      mailSendenAction.handleAction(member);
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler senden der Spendenbescheinigung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
