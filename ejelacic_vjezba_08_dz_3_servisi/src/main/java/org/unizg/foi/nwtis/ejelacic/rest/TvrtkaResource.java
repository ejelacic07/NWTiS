package org.unizg.foi.nwtis.ejelacic.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.dao.PartnerDAO;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.VrstaObracuna;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/tvrtka")
public class TvrtkaResource {

  @Inject
  @ConfigProperty(name = "adresa")
  private String tvrtkaAdresa;
  @Inject
  @ConfigProperty(name = "mreznaVrataKraj")
  private Integer mreznaVrataKraj;
  @Inject
  @ConfigProperty(name = "mreznaVrataRegistracija")
  private Integer mreznaVrataRegistracija;
  @Inject
  @ConfigProperty(name = "mreznaVrataRad")
  private Integer mreznaVrataRad;
  @Inject
  @ConfigProperty(name = "kodZaAdminTvrtke")
  private String kodZaAdminTvrtke;
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;
  @Inject
  @ConfigProperty(name = "idPartnera")
  private Integer idPartner;


  Logger log = Logger.getLogger(TvrtkaResource.class.getName());

  @Inject
  RestConfiguration restConfiguration;

  @Inject
  @ConfigProperty(name = "klijentTvrtkaInfo")
  private String urlRestKlijent;

  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {

    var status = posaljiKomandu("KRAJ XXX", mreznaVrataKraj);

    if (status != null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }



  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStatusId(@PathParam("id") int id) {

    var status =
        posaljiKomandu("STATUS" + " " + this.kodZaAdminTvrtke + " " + id, this.mreznaVrataRad);

    if (status != null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPauza(@PathParam("id") int id) {

    var status = posaljiKomandu("PAUZA " + this.kodZaAdminTvrtke + " " + id, mreznaVrataKraj);

    if (status != null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStart(@PathParam("id") int id) {

    var status = posaljiKomandu("START " + this.kodZaAdminTvrtke + " " + id, mreznaVrataKraj);

    if (status == null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljKraj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKraj() {

    var status = posaljiKomandu("KRAJ " + this.kodZaKraj, mreznaVrataKraj);

    if (status == null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @Path("kraj/info")
  @HEAD
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKrajInfo() {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request;
    try {
      request = HttpRequest.newBuilder().uri(new URI(urlRestKlijent + "api/tvrtka/kraj/info")).GET()
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      int status = response.statusCode();


      if (Response.Status.OK.getStatusCode() == status) {

        return Response.status(Response.Status.OK).build();
      }
    } catch (URISyntaxException | IOException | InterruptedException e) {

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }


  @Path("jelovnik")
  @GET
  @Operation(summary = "Vraća informacije s jelovnicima")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Ne postoji")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response dohvatiJelovnik() {

    var status = posaljiKomandu("JELOVNIK" + " " + this.idPartner, mreznaVrataRad);

    if (status == null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @Path("kartapica")
  @GET
  @Operation(summary = "Vraća informacije s kartama  pića")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Ne postoji")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response dohvatiKartuPica() {

    var status = posaljiKomandu("KARTAPIĆA" + " " + this.idPartner, mreznaVrataRad);

    if (status == null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }


  @Path("jelovnik/{id}")
  @GET
  @Operation(summary = "Vraća jelovnik s id")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response dohvatiJelovnikSIdom(@PathParam("id") int id) {

    var status = posaljiKomandu("JELOVNIK " + id + "", mreznaVrataRad);

    if (status == null) {

      return Response.status(Response.Status.OK).build();
    } else {

      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneri",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
  public Response getPartneri() {

    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      var partneri = partnerDAO.dohvatiSve(true);

      return Response.ok(partneri).status(Response.Status.OK).build();
    } catch (Exception e) {
      log.severe("Greška u getPartneri: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
  public Response getPartner(@PathParam("id") int id) {

    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      var partner = partnerDAO.dohvati(id, true);
      if (partner != null) {

        return Response.ok(partner).status(Response.Status.OK).build();
      } else {
        log.warning("Partner s id=" + id + " nije pronađen");
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      log.severe("Greška u getPartner: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
  public Response postPartner(Partner partner) {

    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      var status = partnerDAO.dodaj(partner);

      if (status) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      log.severe("Greška u postPartner: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }



  @GET
  @Path("/partner/provjera")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dohvatiPartnereNaServeru() {

    var status = posaljiKomandu("POPIS", mreznaVrataRad);

    if (status == null) {
      log.warning("Odgovor je null - greška kod posaljiKomandu");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      List<Partner> partneri = partnerDAO.provjera(status);

      return Response.ok(partneri).build();
    } catch (Exception e) {
      log.severe("Greška u dohvatiPartnereNaServeru: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  @Path("/obracun")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dohvatiObracune(@QueryParam("od") long od, @QueryParam("do") long doVrijeme) {

    String odString = String.valueOf(od);
    String doVrijemeString = String.valueOf(doVrijeme);


    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      List<Obracun> obracuni =
          partnerDAO.obradiObracun(odString, doVrijemeString, VrstaObracuna.SVE);

      return Response.ok(obracuni).build();
    } catch (Exception e) {
      log.severe("Greška u dohvatiObracune: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }



  @GET
  @Path("/obracun/jelo")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dohvatiObracuneJela(@QueryParam("od") String od,
      @QueryParam("do") String doVrijeme) {


    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      List<Obracun> obracuni = partnerDAO.obradiObracun(od, doVrijeme, VrstaObracuna.JELA);

      return Response.ok(obracuni).build();
    } catch (Exception e) {
      log.severe("Greška u dohvatiObracuneJela: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @GET
  @Path("/obracun/pice")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dohvatiObracunePica(@QueryParam("od") String od,
      @QueryParam("do") String doVrijeme) {

    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      List<Obracun> obracuni = partnerDAO.obradiObracun(od, doVrijeme, VrstaObracuna.PIĆA);

      return Response.ok(obracuni).build();
    } catch (Exception e) {
      log.severe("Greška u dohvatiObracunePica: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @GET
  @Path("/obracun/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response dohvatiObracunePartnera(@PathParam("id") int id, @QueryParam("od") String od,
      @QueryParam("do") String doVrijeme) {
    log.info(
        "Pozvana metoda dohvatiObracunePartnera s id=" + id + ", od=" + od + ", do=" + doVrijeme);
    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      List<Obracun> obracuni = partnerDAO.dohvatiObracunPoPartneru(id, od, doVrijeme);

      return Response.ok(obracuni).build();
    } catch (Exception e) {
      log.severe("Greška pri dohvaćanju obračuna: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @POST
  @Path("/obracun")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response dodajObracun(List<Obracun> jsonObracun) {

    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      boolean noviObracun = partnerDAO.dodajObracun(jsonObracun);

      if (noviObracun) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        request = HttpRequest.newBuilder().uri(new URI(urlRestKlijent + "api/tvrtka/obracun")) // ovaj
                                                                                               // dio
                                                                                               // provjeriti
            .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();


        if (Response.Status.OK.getStatusCode() == status) {

          return Response.status(Response.Status.CREATED).build();
        }
      }

    } catch (Exception e) {
      log.severe("Greška pri dodavanju obračuna: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }



  @POST
  @Path("/obracun/ws")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response dodajObracunISaljiNaPosluzitelj(List<Obracun> jsonObracun) {
    log.info(
        "Pozvana metoda dodajObracunISaljiNaPosluzitelj sa " + jsonObracun.size() + " obračuna");
    try (var vezaBP = this.restConfiguration.dajVezu()) {

      var partnerDAO = new PartnerDAO(vezaBP);

      boolean noviObracun = partnerDAO.dodajObracun(jsonObracun);

      if (noviObracun) {
        for (Obracun obracun : jsonObracun) {
          Partner p = partnerDAO.dohvati(obracun.partner(), false);

          if (p != null) {
            String komanda = "OBRAČUNWS " + p.id() + " " + p.sigurnosniKod() + " " + jsonObracun;

            String odgovor = posaljiKomandu(komanda, this.mreznaVrataRad);

            if ("OK".equals(odgovor))
              return Response.status(Response.Status.CREATED).build();
          }
        }
      }
    } catch (Exception e) {
      log.severe("Greška pri dodavanju i slanju obračuna: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    log.warning("Obračun nije dodan ili odgovor nije OK");
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }


  @GET
  @Path("/spava")
  public Response dretvaSpava(@QueryParam("vrijeme") long trajanje) {

    try {
      Thread.sleep(trajanje);
    } catch (InterruptedException e) {
      log.severe("Dretva prekinuta: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.ok().build();
  }


  private String posaljiKomandu(String komanda, Integer mreznaVrata) {
    log.info("Pozvana metoda posaljiKomandu s komandom: " + komanda + " i mrežnim vratima: "
        + mreznaVrata);
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, mreznaVrata);

      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();

      mreznaUticnica.shutdownOutput();

      var linija = in.readLine();

      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
      return linija;
    } catch (IOException e) {
      log.severe("Greška pri slanju komande: " + e.getMessage());
    }
    log.warning("Odgovor je null");
    return null;
  }
}
