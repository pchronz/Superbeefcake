@initRegister = () ->
  validateEmail(email) -> 
      atpos=email.indexOf("@")
      dotpos=email.lastIndexOf(".")
      if atpos<1 or dotpos<atpos+2 or dotpos+2>=email.length 
        return false
      else 
        return true
  $("#submit-button").click(() -> 
      username = $("input[name='username']").val()
      email = $("input[name='email']").val()
      password = $("input[name='password']").val()
      passwordRepeat = $("input[name='passwordRepeat']").val()
      if username.length < 4 
        $("#alert-username").show()
        return false
      else 
        $("#alert-username").hide()
      if not validateEmail(email) 
        $("#alert-email").show()
        return false
      else 
        $("#alert-email").hide()
      if not password == passwordRepeat 
        $("#alert-passwords-match").show()
        return false
      else 
        $("#alert-passwords-match").hide()
      
      if password.length < 4  
        $("#alert-password-length").show()
        return false
      else 
        $("#alert-password-match").hide()
      return true
  )


