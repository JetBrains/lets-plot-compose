package plotSpec

class FrontendExceptionSpec : PlotDemoSpec {
    override fun createRawSpec(): MutableMap<String, Any> {
        return mutableMapOf(
            "kind" to "plot",
            "layers" to listOf(
                mapOf(
                    "geom" to "line",
                    "data" to mapOf(
                        "x" to listOf("a", "b", "c", "d"),
                        "y" to listOf(3, 1, 4, 2)
                    ),
                    "mapping" to mapOf("x" to "x", "y" to "y"),
                    "stat" to "summary",
                    "fun" to "mean",
                    "data_meta" to mapOf(
                        "series_annotations" to listOf(
                            mapOf(
                                "type" to "datetime",
                                "column" to "x"
                            )
                        )
                    )
                )
            )
        )
    }

    override fun createRawSpecList(): List<MutableMap<String, Any>> {

        return listOf(
            createRawSpec(),
        )
    }

}

