package models

case class Annonce(
  id: Long,
  //localisation: Localisation,
  //categorie: Categorie,
  //annonceurType: AnnonceurType,
  //annonceType: AnnonceType,
  //createur: Annonceur,
  titre: String,
  texte: String,
  prix: Option[BigDecimal]
  //photos: Seq[Photo],
  //annonceState: AnnonceState
)