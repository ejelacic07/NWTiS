package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "klijentPartner")
@Path("api/partner")
public interface ServisiPartnerKlijent {

  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getJelovnik(@HeaderParam("korisnik") String korisnickoIme,
      @HeaderParam("lozinka") String lozinka);

}
