(function() { //iife

  // Page components

  var invitedMeetingsList, convenedMeetingsList, formComponent, pageManager = new PageManager();

  window.addEventListener("load", () => {
    pageManager.start();
    pageManager.refresh();
  }, false);


  // Constructors of view components

  function InvitedMeetingsList(alertElement, listElement, listBodyElement) {
    this.alert = alertElement;
    this.list = listElement;
    this.listBody = listBodyElement;

    // Hide the componet
    this.reset = function() {
      this.list.style.visibility = "hidden";
      this.listBody.style.visibility = "hidden";
      this.alert.textContent = "Loading...";
      this.alert.style.visibility = "visible";
    }

    // Retrieve the meetings from the server
    this.show = function() {
      var self = this;
      makeCall("GET", "GetInvitedMeetingsList", null,
        function(req) { // Call back function
          if (req.readyState == XMLHttpRequest.DONE) {
            var message = req.responseText;
            if (req.status == 200) {
              // update the list
              self.update(JSON.parse(req.responseText));
            } else {
              self.alert.textContent = "Server unreachable";
              self.alert.style.visibility = "visible";
            }
          }
        }
      );
    };

    // Update the list
    this.update = function(arrayMeetings) {
      var row, titleCell, dateCell, timeCell, durationCell, maxParticipantsCell, creatorCell;
      if (arrayMeetings.length == 0) {
        this.alert.textContent = "No meetings found";
        this.alert.style.visibility = "visible";
      } else {
        // Empty the table body
        this.listBody.innerHTML = "";

        // Generate the updated list
        var self = this;
        arrayMeetings.forEach(function(meeting) {
          row = document.createElement("tr");

          titleCell = document.createElement("td");
          titleCell.textContent = meeting.title;
          row.appendChild(titleCell);

          dateCell = document.createElement("td");
          dateCell.textContent = meeting.date;
          row.appendChild(dateCell);

          timeCell = document.createElement("td");
          timeCell.textContent = meeting.time;
          row.appendChild(timeCell);

          durationCell = document.createElement("td");
          durationCell.textContent = meeting.duration;
          row.appendChild(durationCell);

          maxParticipantsCell = document.createElement("td");
          maxParticipantsCell.textContent = meeting.maxParticipants;
          row.appendChild(maxParticipantsCell);

          creatorCell = document.createElement("td");
          creatorCell.textContent = meeting.creator;
          row.appendChild(creatorCell);

          self.listBody.appendChild(row);
        });
        this.list.style.visibility = "visible";
        this.listBody.style.visibility = "visible";
        this.alert.style.visibility = "hidden";
      }
    }
  }

  function ConvenedMeetingsList(alertElement, listElement, listBodyElement) {
    this.alert = alertElement;
    this.list = listElement;
    this.listBody = listBodyElement;

    // Hide the componet
    this.reset = function() {
      this.list.style.visibility = "hidden";
      this.listBody.style.visibility = "hidden";
      this.alert.textContent = "Loading...";
      this.alert.style.visibility = "visible";
    }

    // Retrieve the meetings from the server
    this.show = function() {
      var self = this;
      makeCall("GET", "GetConvenedMeetingsList", null,
        function(req) { // Call back function
          if (req.readyState == XMLHttpRequest.DONE) {
            var message = req.responseText;
            if (req.status == 200) {
              // update the list
              self.update(JSON.parse(req.responseText));
            } else {
              self.alert.textContent = "Server unreachable";
              self.alert.style.visibility = "visible";
            }
          }
        }
      );
    };

    // Update the list
    this.update = function(arrayMeetings) {
      var row, titleCell, dateCell, timeCell, durationCell, maxParticipantsCell, participantsCell;
      if (arrayMeetings.length == 0) {
        this.alert.textContent = "No meetings found";
        this.alert.style.visibility = "visible";
      } else {
        // Empty the table body
        this.listBody.innerHTML = "";

        // Generate the updated list
        var self = this;
        arrayMeetings.forEach(function(meeting) {
          row = document.createElement("tr");

          titleCell = document.createElement("td");
          titleCell.textContent = meeting.title;
          row.appendChild(titleCell);

          dateCell = document.createElement("td");
          dateCell.textContent = meeting.date;
          row.appendChild(dateCell);

          timeCell = document.createElement("td");
          timeCell.textContent = meeting.time;
          row.appendChild(timeCell);

          durationCell = document.createElement("td");
          durationCell.textContent = meeting.duration;
          row.appendChild(durationCell);

          participantsCell = document.createElement("td");
          participantsCell.textContent = meeting.participants;
          row.appendChild(participantsCell);

          maxParticipantsCell = document.createElement("td");
          maxParticipantsCell.textContent = meeting.maxParticipants;
          row.appendChild(maxParticipantsCell);

          self.listBody.appendChild(row);
        });
        this.list.style.visibility = "visible";
        this.listBody.style.visibility = "visible";
        this.alert.style.visibility = "hidden";
      }
    }
  }

  function FormComponent(wizardElement, errorElement) {

    this.wizard = wizardElement;
    this.error = errorElement;
    this.attempts = 0;

    // LOAD THE FORM COMPONENT: prepare buttons
    this.load = function(pageManager) {

      // Minimum date and time setup
      var now = new Date(),
      	  minimumDate=now.toISOString().substring(0, 10);
          dateField = this.wizard.querySelector('input[type="date"]');
      dateField.setAttribute("min", minimumDate);
      dateField.addEventListener("change",(e) => {
        if(dateField.value==minimumDate){
          now = new Date(); // Update time
        	var minimumTime = now.getHours()+":"+((now.getMinutes()<10)?"0":"" )+now.getMinutes();
        	this.wizard.querySelector('input[type="time"]').setAttribute("min", minimumTime);
        } else{
        	this.wizard.querySelector('input[type="time"]').removeAttribute("min");
        }
      });

      // Manage next buttons
      Array.from(this.wizard.querySelectorAll("input[type='button'].next")).forEach(btn => {
        btn.addEventListener("click", (e) => {

          // Retrieve the fieldset and validate it
          var eventfieldset = e.target.closest("fieldset"), valid = true;
          for ( var i = 0; i < eventfieldset.elements.length && valid; i++) {
        	if (!eventfieldset.elements[i].checkValidity()) {
              valid = false;
              eventfieldset.elements[i].reportValidity();
            }
          }
          if (valid) {
            this.error.textContent = "Loading...";
            this.error.style.visibility = "visible";
            this.getUsersList();

            this.changeStep(e.target.closest("fieldset"), e.target.closest("fieldset").nextElementSibling);
          }
        }, false);
      });

      // Manage prev buttons
      Array.from(this.wizard.querySelectorAll("input[type='button'].prev")).forEach(btn => {
        btn.addEventListener("click", (e) => {
          this.changeStep(e.target.parentNode, e.target.parentNode.previousElementSibling);
        }, false);
      });

      // Manage submit buttons
      this.wizard.querySelector("input[type='button'].submit").addEventListener('click', (e) => {
        var eventfieldset = e.target.closest("fieldset"),
            maxParticipants = e.target.closest('form').elements["maxParticipants"].value,
            selected=0;
        for (var i = 0; i < eventfieldset.elements.length-3; i++) {
          if (eventfieldset.elements[i].checked) {
            selected++;
          }
        }
        if (selected<=maxParticipants && selected!=0) {
          this.error.style.visibility = "hidden";
          var self = this, form= event.target.closest("form");
          makeCall("POST", 'CreateMeeting', form ,
            function(req) {
              if (req.readyState == XMLHttpRequest.DONE) {
                if (req.status == 200) {
                  // Meeting added: refresh componets
                  pageManager.refresh();
                } else {
                  // Error message
                  self.reset();
                  self.error.textContent = req.responseText;
                  self.error.style.visibility = "visible";
                }
              }
            }
          );
        }else{
          if(selected==0){
            this.error.textContent="Select at least one user!";
          } else{
            this.attempts++;
            if(this.attempts==3){
              this.error.textContent="Three attempts to create a meeting with too many participants, the meeting will not be created";
              e.target.closest('form').reset();
              this.reset();
            }else{
              this.error.textContent="Too many users selected, remove at least "+(selected-maxParticipants)+" users!";
            }
          }
          this.error.style.visibility = "visible";
        }
      });

      // Manage cancel button
      this.wizard.querySelector("input[type='button'].cancel").addEventListener('click', (e) => {
        e.target.closest('form').reset();
        this.reset();
      });
    };

    // GET USERS LIST: retrieve the users from the server
    this.getUsersList = function(){
        var self = this;
        makeCall("GET", "GetUsersList", null,
          function(req) { // Call back function
            if (req.readyState == XMLHttpRequest.DONE) {
              var message = req.responseText;
              if (req.status == 200) {
                // update the list
                self.updateUsersList(JSON.parse(req.responseText));
              } else {
                self.error.textContent = "Server unreachable";
                self.error.style.visibility = "visible";
              }
            }
          }
        );
    };

    // UPDATE USERS LIST: add the checkbox elements to the form
    this.updateUsersList = function(arrayUsers){
      var div, pElem, inputElem, labelElem, listContainer = document.getElementById("usersList");
      if (arrayUsers.length == 0) {
        this.error.textContent = "No users. You can't create a meeting at the moment.";
        this.error.style.visibility = "visible";
      } else {
        // Empty the list
        listContainer.innerHTML = "";

        pElem = document.createElement("p");
        var form = document.getElementById("meetingForm").elements;
        pElem.textContent = "Select at most "+form["maxParticipants"].value +" guests for "+form["title"].value;
        listContainer.appendChild(pElem);

        // Generate the updated list
        var self = this;
        arrayUsers.forEach(function(user) {
          div = document.createElement("div");

          inputElem = document.createElement("input");
          inputElem.setAttribute("type", "checkbox");
          inputElem.setAttribute("name", "guests");
          inputElem.setAttribute("value", user);
          div.appendChild(inputElem);

          labelElem = document.createElement("label");
          labelElem.textContent = user;
          div.appendChild(labelElem);

          listContainer.appendChild(div);
        });
        this.error.style.visibility = "hidden";
      }
    };

    // RESET FORM: go to the first step and reset the form
    this.reset = function() {
      var fieldsets = document.querySelectorAll("#" + this.wizard.id + " fieldset");
      fieldsets[0].hidden = false;
      fieldsets[1].hidden = true;
      this.error.style.visibility = "hidden";
      this.attempts = 0;
    };

    // CHANGE STEP: update the form view
    this.changeStep = function(origin, destination) {
      this.error.style.visibility = "hidden";
      this.error.textContent="";
      origin.hidden = true;
      destination.hidden = false;
    };
  }


  // Manager of the whole page
  function PageManager() {

    // Prepare all the view components
    this.start = function() {
      // Retrieve the username and complete the welcome message
      document.getElementById("username").textContent = sessionStorage.getItem('username').toUpperCase();

      // Prepare other view components
      invitedMeetingsList = new InvitedMeetingsList(
        document.getElementById("invitedMeetingsMessage"),
        document.getElementById("invitedMeetings"),
        document.getElementById("invitedMeetingsBody"));
      convenedMeetingsList = new ConvenedMeetingsList(
        document.getElementById("convenedMeetingsMessage"),
        document.getElementById("convenedMeetings"),
        document.getElementById("convenedMeetingsBody"));

      formComponent = new FormComponent(
        document.getElementById("meetingForm"),
        document.getElementById("errorMessage"));
      formComponent.load(this);
    };

    // Refresh the view componets
    this.refresh = function() {
      invitedMeetingsList.reset();
      convenedMeetingsList.reset();
      invitedMeetingsList.show();
      convenedMeetingsList.show();
      formComponent.reset();
    };
  }
})();
