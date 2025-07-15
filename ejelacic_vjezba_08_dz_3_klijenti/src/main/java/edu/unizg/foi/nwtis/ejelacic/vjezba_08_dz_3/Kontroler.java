/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3;

import java.util.List;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.ws.WebSocketTvrtka;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericType;


@Controller
@Path("tvrtka")
@RequestScoped
@FormAuthenticationMechanismDefinition(loginToContinue = @LoginToContinue(
    loginPage = "/prijavaKorisnika.xhtml", errorPage = "/error.xhtml"))

public class Kontroler {


  Logger log = Logger.getLogger(Kontroler.class.getName());
  GlobalniPodaci gp = new GlobalniPodaci();

  @Inject
  private Models model;

  @Inject
  private BindingResult bindingResult;

  @Inject
  @RestClient
  ServisTvrtkaKlijent servisTvrtka;

  @GET
  @Path("pocetak")
  @View("index.jsp")
  public void pocetak() {}

  @GET
  @Path("admin/kraj")
  @View("status.jsp")
  public void kraj() {
    try {
      int brojObracuna = gp.getBrojObracuna();
      var status = this.servisTvrtka.headPosluziteljKraj().getStatus();

      if (status == 200) {
        WebSocketTvrtka.send("RADI;" + brojObracuna + ";");
      } else {
        WebSocketTvrtka.send("NE RADI;" + brojObracuna + ";");
      }
      this.model.put("status", status);
      dohvatiStatuse();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @GET
  @Path("status")
  @View("status.jsp")
  public void status() {
    dohvatiStatuse();
  }

  @GET
  @Path("admin/status/{id}")
  @View("status.jsp")
  public void statusId(@PathParam("id") int id) {
    int brojObracuna = gp.getBrojObracuna();

    var status = this.servisTvrtka.headPosluziteljStatus(id).getStatus();
    if (status == 200) {
      WebSocketTvrtka.send("RADI;" + brojObracuna + ";");
    } else {
      WebSocketTvrtka.send("NE RADI;" + brojObracuna + ";");
    }
    this.model.put("status", status);
    this.model.put("samoOperacija", true);

  }


  @GET
  @Path("admin/start/{id}")
  @View("status.jsp")
  public void startId(@PathParam("id") int id) {
    int brojObracuna = gp.getBrojObracuna();

    var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
    if (status == 200) {
      WebSocketTvrtka.send("RADI;" + brojObracuna + ";");
    } else {
      WebSocketTvrtka.send("NE RADI;" + brojObracuna + ";");
    }

    this.model.put("status", status);
    this.model.put("samoOperacija", true);

  }



  @GET
  @Path("admin/pauza/{id}")
  @View("status.jsp")
  public void pauzatId(@PathParam("id") int id) {
    int brojObracuna = gp.getBrojObracuna();

    var odgovor = this.servisTvrtka.headPosluziteljPauza(id);
    int status = odgovor.getStatus();
    if (status == 200) {
      WebSocketTvrtka.send("RADI;" + brojObracuna + ";");
    } else {
      WebSocketTvrtka.send("NE RADI;" + brojObracuna + ";");
    }
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
    this.model.put("poruka", "Neispravan id dijela poslužitelja.");

  }


  @GET
  @Path("partner")
  @View("partneri.jsp")
  public void partneri() {
    var odgovor = this.servisTvrtka.getPartneri();
    var status = odgovor.getStatus();
    if (status == 200) {
      var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {});
      this.model.put("status", status);
      this.model.put("partneri", partneri);
    }
  }


  @GET
  @Path("privatno/obracun")
  @View("obracun.jsp")
  public void pregledObracuna(@QueryParam("od") long od, @QueryParam("do") long kraj) {
    var odgovor = servisTvrtka.dohvatiObracune(od, kraj);
    int status = odgovor.getStatus();
    if (status == 200) {
      var obracun = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracun", obracun);
    }
  }

  @GET
  @Path("privatno/obracun/jelo")
  @View("obracun.jsp")
  public void pregledObracunaJelo(@QueryParam("od") String od, @QueryParam("do") String kraj) {

    var odgovor = servisTvrtka.dohvatiObracuneJela(od, kraj);
    int status = odgovor.getStatus();
    if (status == 200) {
      var obracun = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracun", obracun);
    }
  }


  @GET
  @Path("privatno/obracun/pice")
  @View("obracun.jsp")
  public void pregledObracunaPice(@QueryParam("od") String od, @QueryParam("do") String kraj) {

    var odgovor = servisTvrtka.dohvatiObracunePica(od, kraj);
    int status = odgovor.getStatus();
    if (status == 200) {
      var obracun = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracun", obracun);
    }
  }

  @GET
  @Path("privatno/obracun/{id}")
  @View("obracun.jsp")
  public void pregledObracunPartnera(@PathParam("id") int id, @QueryParam("od") String od,
      @QueryParam("do") String kraj) {
    var odgovor = servisTvrtka.dohvatiObracunePartnera(id, od, kraj);
    int status = odgovor.getStatus();
    if (status == 200) {
      var obracun = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("status", status);
      this.model.put("obracun", obracun);
      this.model.put("idPartnera", id);
    }
  }


  @GET
  @Path("admin/partner")
  @View("unosPartnera.jsp")
  public void dodajPartnera() {

  }

  @POST
  @Path("admin/partner")
  @View("unosPartnera.jsp")
  public void dodajPartnera(@FormParam("id") int id, @FormParam("naziv") String naziv,
      @FormParam("vrstaKuhinje") String vrstaKuhinje, @FormParam("adresa") String adresa,
      @FormParam("mreznaVrata") int mreznaVrata, @FormParam("mreznaVrataKraj") int mreznaVrataKraj,
      @FormParam("gpsSirina") float gpsSirina, @FormParam("gpsDuzina") float gpsDuzina,
      @FormParam("sigurnosniKod") String sigurnosniKod, @FormParam("adminKod") String adminKod) {
    Partner partner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj,
        gpsSirina, gpsDuzina, sigurnosniKod, adminKod);

    var odgovor = servisTvrtka.spremiPartnera(partner);
    int status = odgovor.getStatus();

    if (status == 201) {
      model.put("poruka", "Partner uspješno dodan!");
      model.put("status", status);
      model.put("partner", partner);
    } else {
      model.put("poruka", "Greška pri dodavanju partnera!");
      model.put("pogreska", true);
    }
  }

  @GET
  @Path("admin/spavanje")
  @View("spavanje.jsp")
  public void obradiSpavanje(@QueryParam("vrijeme") long trajanje) {
    System.out.println("U metodi obradiSpavanje");
    System.out.println("Trajanje " + trajanje);

    if (trajanje > 0) {
      System.out.print("Ušao sam u if/trajanje");
      long trajanjeUMilisekundama = trajanje * 1000L;
      var odgovor = servisTvrtka.dretvaSpava(trajanjeUMilisekundama);
      int status = odgovor.getStatus();

      System.out.println("Odgovor " + odgovor + " Status " + status);

      if (status == 200) {
        this.model.put("status", status);
        this.model.put("vrijeme", trajanje);
      }
    }
  }



  @GET
  @Path("admin/nadzornaKonzolaTvrtka")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void nadzornaKonzolaTvrtka() {}

  private void dohvatiStatuse() {
    this.model.put("samoOperacija", false);
    var statusT = this.servisTvrtka.headPosluzitelj().getStatus();
    this.model.put("statusT", statusT);
    var statusT1 = this.servisTvrtka.headPosluziteljStatus(1).getStatus();
    this.model.put("statusT1", statusT1);
    var statusT2 = this.servisTvrtka.headPosluziteljStatus(2).getStatus();
    this.model.put("statusT2", statusT2);
  }

}
