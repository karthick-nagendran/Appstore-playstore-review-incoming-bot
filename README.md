# Appstore-playstore-review-incoming-bot
App store and play store incoming Web hook feed for Slack and Mattermost

## App reviews bot for Slack and Mattermost

### iOS
Apple provides an RSS feed to retrieve user reviews from the iOS app store. This feed makes it quite easy to download reviews for further analysis. Here’s some code to get you started.

What you need to know before you start: - you can only go back 10 pages for a given filter, so may want to save old reviews locally. - you need to retrieve by app store country.

To find the reviews just insert the right values to the following URL format:

https://itunes.apple.com/CODE/rss/customerreviews/page=1/id=APPID/sortby=mostrecent/FILETYPE
CODE is an App Store territory code. The complete list is available here.

APPID is a 9 digit number unique for each app.

FILETYPE can be xml or json.

THANKS

https://rstudio-pubs-static.s3.amazonaws.com/144219_cdfba94f3f4d42a1b582c0b69a60cf78.html

Mattermost Incoming Webhooks

https://docs.mattermost.com/developer/webhooks-incoming.html

### Android

Android reviews can be retrive directly from Google API but another simple solution to retive the Android review is use the below API

https://still-plateau-10039.herokuapp.com/reviews?id={App_bundle_ID}
