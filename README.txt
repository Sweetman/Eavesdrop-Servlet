Implemented a Servlet to query and present data from http://eavesdrop.openstack.org/

The eavesdrop site is a site that provides public access to the IRC chat logs and meeting logs for various OpenStack projects.

The Servlet should handle three query parameters which would be used to construct a query for accessing data from the eavesdrop website. These parameters are: type, project, year. The allowed values for the ‘type’ parameter are ‘irclogs’ and ‘meetings’. If ‘type’ is irclogs, you should ignore the year parameter.

In addition to the above three query parameters, the Servlet also handles two more query parameters username and session. The username parameter can take any value, whereas the session parameter can take only two values: start and end.

The username and session parameters is used for managing user sessions. A session for a particular user should be started when the session parameter is passed in with the value of start. All the subsequent interactions should be tracked as being part of this session. The session is terminated when the session parameter is passed in with the value of end.

Used Cookies to maintain and track user sessions.