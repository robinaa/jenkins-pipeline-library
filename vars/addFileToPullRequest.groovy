#!/usr/bin/groovy

/**
 * Adds a comment on the PR in github.
 * I could not find an API which allows me to upload the actual file.
 * @param filename - The file who's content we wish to leave in the comment section. Should not be empty.
 * @param pr - The pull-request id, should be int.
 * @param project - The github project , should be string.
 */
def call(String filename, String pr, String project) {
    withCredentials([[$class: 'StringBinding', credentialsId: 'github_oath_token', variable: 'GITHUB_ACCESS_TOKEN']]) {
        def githubToken = "${GITHUB_ACCESS_TOKEN}"
        def apiUrl = new URL("https://api.github.com/repos/${project}/issues/${pr}/comments")

        try {
            def HttpURLConnection connection = apiUrl.openConnection()
            if (githubToken.length() > 0) {
                connection.setRequestProperty("Authorization", "Bearer ${githubToken}")
            }
            connection.setRequestMethod("POST")
            connection.setDoOutput(true)
            connection.connect()
            
            // replaceAll("[^\\x00-\\x7F]", "") == stip non ascii characters
            // .readLines().join('<br />').trim() == github comments are structured using html, not linebreaks

            String fileContents = new File("${filename}").readLines().join('<br />').trim().replaceAll("[^\\x00-\\x7F]", "")
            def body = "{\"body\":\"${fileContents}\"}"

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())
            writer.write(body)
            writer.flush()

            // execute the POST request
            new InputStreamReader(connection.getInputStream())
            connection.disconnect()
        } catch (err) {
            echo "ERROR  ${err}"
        }
    }
}
