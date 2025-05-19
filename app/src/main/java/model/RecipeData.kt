package model

data class RecipeData(
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>
)