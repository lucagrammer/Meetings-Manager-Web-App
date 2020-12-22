(function() { //iife

  // Page components

  var loginComponent, registerComponent, pageManager = new PageManager();

  window.addEventListener("load", () => {
    pageManager.start();
  }, false);


  // Constructors of view components

  function LoginComponent(wizardElement) {

    this.wizard = wizardElement;
    this.error = wizardElement.querySelector('.errorMessage');

    this.load = function(pageManager) {

      // Manage the change view button
      this.wizard.querySelector('.changeView').addEventListener("click", (e) => {
        this.reset();
        pageManager.changeView(e.target.closest("form"),e.target.closest("form").nextElementSibling);
      }, false);

      // Manage the login button
      this.wizard.querySelector('.submit').addEventListener('click', (e) => {
        var form = e.target.closest("form");

        // Validate the form
        if (form.checkValidity()) {
          this.setError("Checking...");
          this.doLogin(form);
        } else {
          form.reportValidity();
        }
      });
    };

    this.doLogin = function(form) {
      var self = this;
      makeCall("POST", 'CheckCredentials', form,
        function(req) {
          if (req.readyState == XMLHttpRequest.DONE) {
            var message = req.responseText;
            switch (req.status) {
              case 200:
                sessionStorage.setItem('username', message);
                window.location.href = "Home.html";
                break;
              default:
                self.setError(message);
                break;
            }
          }
        }
      );
    };

    this.reset = function() {
      this.clearError();
      this.wizard.reset();
    };

    this.clearError = function() {
      this.error.style.visibility = "hidden";
      this.error.textContent="";
    };

    this.setError = function(text){
      this.error.style.visibility = "visible";
      this.error.textContent=text;
    }
  }

  function RegisterComponent(wizardElement) {

    this.wizard = wizardElement;
    this.error = wizardElement.querySelector('.errorMessage');

    this.load = function(pageManager) {

      // Manage the change view button
      this.wizard.querySelector('.changeView').addEventListener("click", (e) => {
        this.reset();
        pageManager.changeView(e.target.closest("form"),e.target.closest("form").previousElementSibling);
      }, false);

      // Manage the login button
      this.wizard.querySelector('.submit').addEventListener('click', (e) => {
        this.clearError();
        var form = e.target.closest("form");
        
        if(form.checkValidity()){
          if(form.elements["password"].value!=form.elements["passwordR"].value){
            this.setError("Passwords do not match");
          }else{
            var self = this, username = form.elements["username"].value;
            makeCall("POST", 'CheckUsername', form,
              function(req) {
                if (req.readyState == XMLHttpRequest.DONE) {
                  if (req.status == 200) {
                    self.clearError();
                    self.createAccount(form);
                  } else {
                    // Error message
                    self.setError("The username \""+username +"\" is not available");
                    form.elements["username"].value="";
                  }
                }
              },
            false);
          }
        }else{
          form.reportValidity();
        }
      });
    };

    this.createAccount = function(form) {
      var self = this;
      makeCall("POST", 'CreateAccount', form ,
        function(req) {
          if (req.readyState == XMLHttpRequest.DONE) {
            if (req.status == 200) {
              self.clearError();
              loginComponent.doLogin(form);
            } else {
              // Error message
              self.setError("Incorrect or missing parameters");
            }
          }
        },
      false);
    }

    this.reset = function() {
      this.clearError();
      this.wizard.reset();
    };

    this.clearError = function() {
      this.error.style.visibility = "hidden";
      this.error.textContent="";
    };

    this.setError = function(text){
      this.error.style.visibility = "visible";
      this.error.textContent=text;
    }
  }


  // Manager of the whole page
  function PageManager() {

    // Prepare all the view components
    this.start = function() {
      loginComponent = new LoginComponent(
        document.getElementById("loginForm"));
      loginComponent.load(this);

      registerComponent = new RegisterComponent(
        document.getElementById("registerForm"));
      registerComponent.load(this);

      this.changeView(document.getElementById("registerForm"),document.getElementById("loginForm"));
    };

    this.changeView = function(origin, destination) {
      origin.hidden = true;
      destination.hidden = false;
    };
  }
})();
