/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.AllgemeineVar;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.gui.control.FormularfeldControl;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatJJJJ;
import de.jost_net.JVerein.util.JVDateFormatMM;
import de.jost_net.JVerein.util.JVDateFormatTT;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import jonelo.NumericalChameleon.SpokenNumbers.GermanNumber;

public class FormularAnzeigeAction implements Action
{

  @SuppressWarnings("deprecation")
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Formular formular = null;

    if (context != null && (context instanceof Formular))
    {
      formular = (Formular) context;
      try
      {
        if (formular.isNewObject())
        {
          throw new ApplicationException(
              "Vor der Anzeige des Formulars muss dieses gespeichert werden!");
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException(
            "Fehler bei der Anzeige eines Formulars", e);
      }
    }
    else
    {
      throw new ApplicationException("Kein Formular zur Anzeige ausgew�hlt");
    }
    try
    {
      final File file = File.createTempFile("formular", ".pdf");
      Mitglied m = (Mitglied) Einstellungen.getDBService()
          .createObject(Mitglied.class, null);

      Map<String, Object> map = new MitgliedMap().getMap(m, null);

      Lastschrift ls = (Lastschrift) Einstellungen.getDBService()
          .createObject(Lastschrift.class, null);
      map = new LastschriftMap().getMap(ls, map);

      map = new AllgemeineMap().getMap(map);
      // Get current zaehler
      map.put(AllgemeineVar.ZAEHLER.getName(), formular.getZaehler());
      map.put(FormularfeldControl.EMPFAENGER,
          "Herr\nDr. Willi Wichtig\nTestgasse 1\n12345 Testenhausen");
      map.put("Betrag in Worten", GermanNumber.toString(1234 + 15));
      map.put(FormularfeldControl.ID, "444");
      map.put(FormularfeldControl.EXTERNEMITGLIEDSNUMMER, "9999");
      map.put(FormularfeldControl.ANREDE, "Herrn");
      map.put(FormularfeldControl.TITEL, "Dr.");
      map.put(FormularfeldControl.NAME, "Wichtig");
      map.put(FormularfeldControl.VORNAME, "Willi");
      map.put(FormularfeldControl.ADRESSIERUNGSZUSATZ, "Hinterhaus");
      map.put(FormularfeldControl.STRASSE, "Testgasse 1");
      map.put(FormularfeldControl.PLZ, "12345");
      map.put(FormularfeldControl.ORT, "Testenhausen");
      map.put(FormularfeldControl.ZAHLUNGSRHYTMUS, "j�hrlich");
      map.put(FormularfeldControl.ZAHLUNGSRHYTHMUS, "j�hrlich");
      map.put(FormularfeldControl.KONTOINHABER, "Wichtig");
      map.put(FormularfeldControl.GEBURTSDATUM, new Date());
      map.put(FormularfeldControl.GESCHLECHT, GeschlechtInput.MAENNLICH);
      map.put(FormularfeldControl.TELEFONPRIVAT, "01234/56789");
      map.put(FormularfeldControl.TELEFONDIENSTLICH, "01234/55555");
      map.put(FormularfeldControl.HANDY, "0160/1234567");
      map.put(FormularfeldControl.EMAIL, "willi.wichtig@jverein.de");
      map.put(FormularfeldControl.EINTRITT, new Date());
      map.put(FormularfeldControl.BEITRAGSGRUPPE, "Erwachsene");
      map.put(FormularfeldControl.AUSTRITT, new Date());
      map.put(FormularfeldControl.KUENDIGUNG, new Date());
      map.put(FormularfeldControl.ZAHLUNGSWEG,
          "Abbuchung von Konto 1234567, BLZ: 10020030");
      map.put(FormularfeldControl.TAGESDATUM,
          new JVDateFormatTTMMJJJJ().format(new Date()));
      map.put(FormularfeldControl.TAGESDATUMTT, new JVDateFormatTT());
      map.put(FormularfeldControl.TAGESDATUMMM, new JVDateFormatMM());
      map.put(FormularfeldControl.TAGESDATUMJJJJ, new JVDateFormatJJJJ());

      Spendenbescheinigung spb = (Spendenbescheinigung) Einstellungen
          .getDBService().createObject(Spendenbescheinigung.class, null);
      map = new SpendenbescheinigungMap().getMap(spb, map);
      map.put(SpendenbescheinigungVar.SPENDEDATUM.getName(), "15.12.2008");
      map.put("Buchungsdatum", new Date());
      map.put(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName(),
          "17.12.2008");
      map.put("Tagesdatum", new JVDateFormatTTMMJJJJ().format(new Date()));
      map.put(SpendenbescheinigungVar.SPENDENZEITRAUM.getName(),
          "13.02.2008 bis 12.11.2008");

      StringBuilder bl = new StringBuilder();
      final String newLineStr = "\n";
      final int colDatumLen = 10;
      final int colArtLen = 27;
      final int colVerzichtLen = 17;
      final int colBetragLen = 11;
      bl.append(StringTool.rpad(" ", colDatumLen));
      bl.append("  ");
      bl.append(StringTool.rpad(" ", colArtLen));
      bl.append("  ");
      bl.append(StringTool.rpad("Verzicht auf", colVerzichtLen));
      bl.append("  ");
      bl.append(StringTool.rpad(" ", colBetragLen));
      bl.append(newLineStr);

      bl.append(StringTool.rpad("Datum der ", colDatumLen));
      bl.append("  ");
      bl.append(StringTool.rpad("Art der", colArtLen));
      bl.append("  ");
      bl.append(StringTool.rpad("die Erstattung", colVerzichtLen));
      bl.append("  ");
      bl.append(StringTool.rpad(" ", colBetragLen));
      bl.append(newLineStr);

      bl.append(StringTool.rpad("Zuwendung", colDatumLen));
      bl.append("  ");
      bl.append(StringTool.rpad("Zuwendung", colArtLen));
      bl.append("  ");
      bl.append(StringTool.rpad("von Aufwendungen", colVerzichtLen));
      bl.append("  ");
      bl.append(StringTool.rpad(StringTool.lpad("Betrag", 8), colBetragLen));
      bl.append(newLineStr);

      bl.append(StringTool.rpad("-", colDatumLen, "-"));
      bl.append("  ");
      bl.append(StringTool.rpad("-", colArtLen, "-"));
      bl.append("  ");
      bl.append(StringTool.rpad("-", colVerzichtLen, "-"));
      bl.append("  ");
      bl.append(StringTool.rpad("-", colBetragLen, "-"));
      bl.append(newLineStr);

      // Buchung 1
      bl.append(StringTool.rpad("13.02.2008", colDatumLen));
      bl.append("  ");
      bl.append(StringTool.rpad("Beitrag", colArtLen));
      bl.append("  ");
      bl.append(StringTool.rpad(StringTool.lpad("nein", colVerzichtLen / 2 - 2),
          colVerzichtLen));
      bl.append("  ");
      String str = Einstellungen.DECIMALFORMAT.format(15.0);
      bl.append(StringTool.lpad(str, colBetragLen));
      bl.append(newLineStr);
      // Buchung 2
      bl.append(StringTool.rpad("12.11.2008", colDatumLen));
      bl.append("  ");
      bl.append(StringTool.rpad("Spende", colArtLen));
      bl.append("  ");
      bl.append(StringTool.rpad(StringTool.lpad("ja", colVerzichtLen / 2 - 2),
          colVerzichtLen));
      bl.append("  ");
      str = Einstellungen.DECIMALFORMAT.format(1234.96);
      bl.append(StringTool.lpad(str, colBetragLen));
      bl.append(newLineStr);

      bl.append(StringTool.rpad("-", colDatumLen, "-"));
      bl.append("  ");
      bl.append(StringTool.rpad("-", colArtLen, "-"));
      bl.append("  ");
      bl.append(StringTool.rpad("-", colVerzichtLen, "-"));
      bl.append("  ");
      bl.append(StringTool.rpad("-", colBetragLen, "-"));
      bl.append(newLineStr);

      bl.append(StringTool.rpad("Gesamtsumme:",
          colDatumLen + 2 + colArtLen + 2 + colVerzichtLen));
      bl.append("  ");
      str = Einstellungen.DECIMALFORMAT.format(15.0 + 1234.96);
      bl.append(StringTool.lpad(str, colBetragLen));
      bl.append(newLineStr);
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE.getName(), bl.toString());

      StringBuilder bl_daten = new StringBuilder();
      bl_daten.append("13.02.2008");
      bl_daten.append(newLineStr);
      bl_daten.append("12.11.2008");
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_DATEN.getName(), bl_daten.toString());

      StringBuilder bl_art = new StringBuilder();
      bl_art.append("Beitrag");
      bl_art.append(newLineStr);
      bl_art.append("Spende");
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_ART.getName(), bl_art.toString());

      StringBuilder bl_verzicht = new StringBuilder();
      bl_verzicht.append("nein");
      bl_verzicht.append(newLineStr);
      bl_verzicht.append("ja");
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_VERZICHT.getName(), bl_verzicht.toString());

      StringBuilder bl_betrag = new StringBuilder();
      str = Einstellungen.DECIMALFORMAT.format(15.0);
      bl_betrag.append(StringTool.lpad(str, colBetragLen));
      bl_betrag.append(newLineStr);
      str = Einstellungen.DECIMALFORMAT.format(1234.96);
      bl_betrag.append(StringTool.lpad(str, colBetragLen));
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_BETRAG.getName(), bl_betrag.toString());

      map.put(SpendenbescheinigungVar.BEZEICHNUNGSACHZUWENDUNG.getName(),
          "gebrauchter Tisch");
      map.put(SpendenbescheinigungVar.HERKUNFTSACHZUWENDUNG.getName(),
          HerkunftSpende.get(1));
      map.put(SpendenbescheinigungVar.UNTERLAGENWERTERMITTUNG.getName(),
          "Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor.");

      // Mitgliedskonto

      ArrayList<Date> buda = new ArrayList<>();
      ArrayList<String> zg = new ArrayList<>();
      ArrayList<String> zg1 = new ArrayList<>();
      ArrayList<Double> nettobetrag = new ArrayList<>();
      ArrayList<String> steuersatz = new ArrayList<>();
      ArrayList<Double> steuerbetrag = new ArrayList<>();
      ArrayList<Double> betrag = new ArrayList<>();
      ArrayList<Double> ist = new ArrayList<>();
      ArrayList<Double> differenz = new ArrayList<>();

      // Buchung 1
      buda.add(new Date());
      zg.add("Testverwendungszweck");
      zg1.add("Testverwendungszweck");
      
      DecimalFormat format = new DecimalFormat("0");
      CurrencyFormatter formatter = new CurrencyFormatter("%",format);

      double steuer = 7d;
      steuersatz.add("("+formatter.format(steuer)+")");
      double netto = 200d / (1d + (steuer / 100d));
      nettobetrag.add(netto);
      steuerbetrag.add(200d - netto);
      betrag.add(200d);
      ist.add(0d);
      differenz.add(-200d);
      
      // Buchung 2
      buda.add(new Date());
      zg.add("2. Verwendungszweck");
      zg1.add("2. Verwendungszweck");
      steuer = 19d;
      steuersatz.add("("+formatter.format(steuer)+")");
      netto = 49.99d / (1d + (steuer / 100d));
      nettobetrag.add(netto);
      steuerbetrag.add(49.99d - netto);
      betrag.add(49.99d);
      ist.add(10d);
      differenz.add(-39.99d);
      
      // Summe
      if (Einstellungen.getEinstellung().getOptiert())
      {
        zg1.add("Rechnungsbetrag inkl. USt.");
        zg.add("Rechnungsbetrag inkl. USt.");
      }
      else
      {
        zg1.add("Summe");
        zg.add("Summe");
      }
      betrag.add(249.99d);
      differenz.add(239.99d);
      ist.add(10d);

      map.put(RechnungVar.STAND.getName(), -39.99d);
      map.put(RechnungVar.QRCODE_SUMME.getName(), "");
      map.put(RechnungVar.DATUM.getName(), new Date());
      map.put(RechnungVar.ANREDE.getName(), "Herrn");
      map.put(RechnungVar.TITEL.getName(), "Dr.");
      map.put(RechnungVar.NAME.getName(), "Wichtig");
      map.put(RechnungVar.VORNAME.getName(), "Willi");
      map.put(RechnungVar.STRASSE.getName(), "Testgasse 1");
      map.put(RechnungVar.ADRESSIERUNGSZUSATZ.getName(), "Hinterhaus");
      map.put(RechnungVar.PLZ.getName(), "12345");
      map.put(RechnungVar.ORT.getName(), "Testenhausen");
      map.put(RechnungVar.STAAT.getName(), "DEUTSCHLAND");
      map.put(RechnungVar.GESCHLECHT.getName(), GeschlechtInput.MAENNLICH);
      map.put(RechnungVar.ANREDE_DU.getName(), "");
      map.put(RechnungVar.ANREDE_FOERMLICH.getName(), "");
      map.put(RechnungVar.PERSONENART.getName(), "");
      map.put(RechnungVar.MANDATID.getName(), "105");
      map.put(RechnungVar.MANDATDATUM.getName(), new Date());
      map.put(RechnungVar.BIC.getName(), "NORSDE51XXX");
      map.put(RechnungVar.IBAN.getName(), "DE27100777770209299700");
      map.put(RechnungVar.IBANMASKIERT.getName(),
          VarTools.maskieren("DE27100777770209299700"));

      map.put(RechnungVar.BUCHUNGSDATUM.getName(), buda.toArray());
      map.put(RechnungVar.ZAHLUNGSGRUND.getName(), zg.toArray());
      map.put(RechnungVar.NETTOBETRAG.getName(), nettobetrag.toArray());
      map.put(RechnungVar.STEUERSATZ.getName(), steuersatz.toArray());
      map.put(RechnungVar.STEUERBETRAG.getName(), steuerbetrag.toArray());
      map.put(RechnungVar.BETRAG.getName(), betrag.toArray());
      map.put(RechnungVar.IST.getName(), ist.toArray());
      map.put(RechnungVar.DIFFERENZ.getName(), differenz.toArray());
      map.put(RechnungVar.SUMME_OFFEN.getName(), 700);
      map.put(RechnungVar.QRCODE_INTRO.getName(),
          Einstellungen.getEinstellung().getQRCodeIntro());
      map.put(RechnungVar.EMPFAENGER.getName(),
          "Herr\nDr. Willi Wichtig\nTestgasse 1\n12345 Testenhausen");
      map.put(RechnungVar.NUMMER.getName(), "0001");
      map.put(RechnungVar.ZAHLUNGSWEGTEXT.getName(),
          Einstellungen.getEinstellung().getRechnungTextUeberweisung());
      FormularAufbereitung fab = new FormularAufbereitung(file, false, false);
      fab.writeForm(formular, map);
      fab.showFormular();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
    catch (IOException e)
    {
      throw new ApplicationException(e);
    }
    catch (Exception e)
    {
      Logger.error("Fehler bei der Anzeige eines Formulares", e);
      throw new ApplicationException(e);
    }
  }
}
