class isChecked {
    fun validEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validPassword(password: String): Boolean {
        val specialCharRegex = Regex(".*[!@#\$%^&*(),.?\":{}|<>].*")
        return password.length >= 6 && specialCharRegex.containsMatchIn(password)
    }
}
