# Repair time management

>Project's specifications can be found in the file designDocument.pdf in italian languages.

The goal of the application is to allow the user to keep track of the time spent by a customer to carry out a repair and at the same time, at the end of the intervention, to be able to enter a brief description of the work done and have the customer sign it as a form of acceptance.
At the end of the work, once the customer has signed and accepted, the application will insert the image of the customer's signature in Google Drive and an event in Google Calendar with:
  1. start and end times based on the work period
  2. the job description provided
  3. Attached is the link to the customer's signature previously saved on Google Drive
 
In order to use this application you have to configure a Google project by setting the package name for the app and the SHA1 certificate.
You can find more info at: https://developers.google.com/identity/sign-in/android/start-integrating
You have also to enable Google Drive and Google Calendar API for the same project on google developer console, https://console.developers.google.com.
