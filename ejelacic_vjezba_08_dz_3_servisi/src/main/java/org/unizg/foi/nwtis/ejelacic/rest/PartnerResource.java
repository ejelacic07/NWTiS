package org.unizg.foi.nwtis.ejelacic.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.dao.KorisnikDAO;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/partner")
public class PartnerResource {

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
  @ConfigProperty(name = "mreznaVrataRadPartner")
  private Integer mreznaVrataRad;
  @Inject
  @ConfigProperty(name = "kodZaAdminPartnera")
  private String kodZaAdminTvrtke;

  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  @Inject
  @ConfigProperty(name = "idPartnera")
  private Integer idPartnera;


  @Inject
  @ConfigProperty(name = "adresaPartner")
  private String adresaPartnera;


  @Inject
  RestConfiguration restConfiguration;

  Logger log = Logger.getLogger(this.getClass().getName());

  @HEAD
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  public Response provjera() {
    return Response.ok().build();
  }

  @HEAD
  @Path("/status/{id}")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "")})
  public Response status(@PathParam("id") String id) {
    String status = posaljiKomandu("KRAJ XXX", mreznaVrataKraj);
    if (status != null) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  @HEAD
  @Path("/pauza/{id}")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "")})
  public Response pauza(@PathParam("id") String id) {
    String komanda = "PAUZA " + kodZaAdminTvrtke + " " + id;
    String odgovor = posaljiKomandu(komanda, mreznaVrataKraj);

    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  @HEAD
  @Path("/start/{id}")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Interna pogreška")})
  public Response start(@PathParam("id") String id) {
    String komanda = "START " + kodZaAdminTvrtke + " " + id;
    String odgovor = posaljiKomandu(komanda, mreznaVrataKraj);

    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  @HEAD
  @Path("/kraj")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Interna pogreška")})
  public Response kraj() {
    String komanda = "KRAJ " + kodZaKraj;
    String odgovor = posaljiKomandu(komanda, mreznaVrataKraj);

    if (odgovor.startsWith("OK")) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  @GET
  @Path("/jelovnik")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJelovnik(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean korisnikOk = korisnikDAO.provjeriKorisnika(korisnickoIme, lozinka);

      if (!korisnikOk) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Exception e) {
      log.severe("getJelovnik() -> Greška u provjeri korisnika: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    String komanda = "JELOVNIK " + this.idPartnera;
    String odgovor = posaljiKomandu(komanda, mreznaVrataRad);

    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok(odgovor.substring(3)).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @GET
  @Path("/kartapica")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Produces(MediaType.APPLICATION_JSON)
  public Response getKartaPica(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {
    String komanda = "KARTAPIĆA " + this.idPartnera;
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean korisnikOk = korisnikDAO.provjeriKorisnika(korisnickoIme, lozinka);

      if (!korisnikOk) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Exception e) {

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    String odgovor = posaljiKomandu(komanda, mreznaVrataRad);

    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok(odgovor.substring(3)).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @GET
  @Path("/narudzba")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = ""),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Produces(MediaType.APPLICATION_JSON)
  public Response getNarudzba(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {
    String komanda = "STANJE " + idPartnera;
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean korisnikOk = korisnikDAO.provjeriKorisnika(korisnickoIme, lozinka);
      if (!korisnikOk) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    String odgovor = posaljiKomandu(komanda, mreznaVrataRad);

    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.ok(odgovor.substring(3)).build();
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @GET
  @Path("/korisnik")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Produces(MediaType.APPLICATION_JSON)
  public Response getKorisnici() {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      List<Korisnik> sviKorisnici = korisnikDAO.dajSveKorisnike();

      if (sviKorisnici != null)
        return Response.ok(sviKorisnici).build();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      log.severe("getKorisnici() -> Greška: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  @Path("/korisnik/{id}")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = " "),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Produces(MediaType.APPLICATION_JSON)
  public Response getKorisnik(@PathParam("id") String id) {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      Korisnik korisnik = korisnikDAO.dajKorsnika(id);

      if (korisnik == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      } else {
        return Response.ok(korisnik.toString()).build();
      }
    } catch (Exception e) {
      log.severe("getKorisnik() -> Greška: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  @Path("/spava")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  public Response spava(@QueryParam("vrijeme") long vrijeme) {
    try {

      Thread.sleep(vrijeme);
    } catch (InterruptedException e) {

      Thread.currentThread().interrupt();
      return Response.serverError().build();
    }
    return Response.ok().build();
  }

  @POST
  @Path("/narudzba")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "409", description = ""),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  public Response novaNarudzba(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean korisnikOk = korisnikDAO.provjeriKorisnika(korisnickoIme, lozinka);
      if (!korisnikOk) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Exception e) {

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    String komanda = "NARUDŽBA " + korisnickoIme;
    String odgovor = posaljiKomandu(komanda, mreznaVrataRad);
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  @POST
  @Path("/jelo")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Consumes(MediaType.TEXT_PLAIN)
  public Response dodajJelo(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka, @HeaderParam("idArtikla") String idJela,
      @HeaderParam("kolicina") Double kolicina) {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean korisnikOk = korisnikDAO.provjeriKorisnika(korisnickoIme, lozinka);


      if (!korisnikOk) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Exception e) {

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    String komanda = "JELO " + korisnickoIme + " " + idJela + " " + kolicina;
    String odgovor = posaljiKomandu(komanda, mreznaVrataRad);
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  @POST
  @Path("/pice")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "409", description = " "),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Consumes(MediaType.TEXT_PLAIN)
  public Response dodajPice(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka, @HeaderParam("idArtikla") String idPica,
      @HeaderParam("kolicina") Integer kolicina) {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean korisnikOk = korisnikDAO.provjeriKorisnika(korisnickoIme, lozinka);

      if (!korisnikOk) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Exception e) {

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    String komanda = "PIĆE " + korisnickoIme + " " + idPica + " " + kolicina;
    String odgovor = posaljiKomandu(komanda, mreznaVrataRad);
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  @POST
  @Path("/racun")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "409", description = " "),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  public Response racun(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka) {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean korisnikOk = korisnikDAO.provjeriKorisnika(korisnickoIme, lozinka);

      if (!korisnikOk) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
    } catch (Exception e) {
      log.severe("racun() -> Greška: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    String komanda = "RAČUN " + korisnickoIme;
    String odgovor = posaljiKomandu(komanda, mreznaVrataRad);
    if (odgovor == null) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
    if (odgovor.startsWith("OK")) {
      return Response.status(Response.Status.CREATED).build();
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  @POST
  @Path("/korisnik")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješna operacija"),
      @APIResponse(responseCode = "409", description = " "),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Consumes(MediaType.APPLICATION_JSON)
  public Response dodajKorisnika(Korisnik korisnik) {
    try {
      KorisnikDAO korisnikDAO = new KorisnikDAO(restConfiguration.dajVezu());
      boolean dodan = korisnikDAO.dodajKorisnika(korisnik);

      if (!dodan) {
        return Response.status(Response.Status.CONFLICT).build();
      } else {
        return Response.status(Response.Status.CREATED).build();
      }
    } catch (Exception e) {
      log.severe("dodajKorisnika() -> Greška: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  private String posaljiKomandu(String komanda, Integer mreznaVrata) {
    try {
      var mreznaUticnica = new Socket(this.adresaPartnera, mreznaVrata);
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
      log.severe("posaljiKomandu() -> Greška: " + e.getMessage());
      return null;
    }
  }

}
